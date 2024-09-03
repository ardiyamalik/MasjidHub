package org.d3if0140.masjidhub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.d3if0140.masjidhub.databinding.ActivityAdminVerifBinding

class AdminVerifActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminVerifBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var masjidListAdapter: MasjidListAdapter
    private val masjidList = mutableListOf<String>()
    private val alamatList = mutableListOf<String>()
    private val masjidDataList = mutableListOf<Map<String, Any?>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminVerifBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        masjidListAdapter = MasjidListAdapter(this, masjidList, alamatList)
        binding.masjidListView.adapter = masjidListAdapter

        fetchMasjidData()

        binding.masjidListView.setOnItemClickListener { _, _, position, _ ->
            val masjidData = masjidDataList[position]
            showVerificationDialog(masjidData)
        }

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
        }
    }

    private fun fetchMasjidData() {
        firestore.collection("user")
            .whereEqualTo("verified", false)
            .get()
            .addOnSuccessListener { result ->
                handleMasjidData(result)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleMasjidData(result: QuerySnapshot) {
        masjidList.clear()
        alamatList.clear()
        masjidDataList.clear()
        for (document in result) {
            val masjidId = document.id
            val masjidData = document.data
            val namaMasjid = masjidData["nama"] as? String ?: "Tidak diketahui"
            val alamat = masjidData["alamat"] as? String ?: "Alamat tidak tersedia"
            masjidList.add(namaMasjid)
            alamatList.add(alamat)
            masjidDataList.add(masjidData)
        }
        masjidListAdapter.notifyDataSetChanged()
    }

    class MasjidListAdapter(context: Context, private val masjidList: List<String>, private val alamatList: List<String>) :
        ArrayAdapter<String>(context, R.layout.item_masjid_list, masjidList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_masjid_list, parent, false)
            val namaMasjidTextView = view.findViewById<TextView>(R.id.text_view_nama_masjid)
            val alamatMasjidTextView = view.findViewById<TextView>(R.id.text_view_alamat_masjid)

            namaMasjidTextView.text = masjidList[position]
            alamatMasjidTextView.text = alamatList[position]

            return view
        }
    }

    private fun showVerificationDialog(masjidData: Map<String, Any?>) {
        val masjidName = masjidData["nama"] as? String ?: "Tidak diketahui"
        val alamat = masjidData["alamat"] as? String ?: "Alamat tidak tersedia"
        val kodePos = masjidData["kodePos"] as? String ?: "Kode Pos tidak tersedia"
        val teleponMasjid = masjidData["teleponMasjid"] as? String ?: "Telepon tidak tersedia"
        val namaKetua = masjidData["namaKetua"] as? String ?: "Nama Ketua tidak tersedia"
        val teleponKetua = masjidData["teleponKetua"] as? String ?: "Telepon Ketua tidak tersedia"
        val email = masjidData["email"] as? String ?: "Email tidak tersedia"
        val ktpUrl = masjidData["ktpKetuaUrl"] as? String ?: ""
        val latitude = masjidData["latitude"] as? String ?: ""
        val longitude = masjidData["longitude"] as? String ?: ""

        val message = """
        Nama : $masjidName
        Alamat: $alamat
        Kode Pos: $kodePos
        Telepon Masjid: $teleponMasjid
        Nama Ketua: $namaKetua
        Telepon Ketua: $teleponKetua
        Email: $email
        latitude: $latitude
        longitude: $longitude
        
        Apakah Anda yakin ingin memverifikasi masjid ini?
    """.trimIndent()

        val dialogView = layoutInflater.inflate(R.layout.dialog_verif_masjid, null)
        val messageTextView = dialogView.findViewById<TextView>(R.id.text_view_message)
        val ktpImageView = dialogView.findViewById<ImageView>(R.id.image_view_ktp)

        messageTextView.text = message
        if (ktpUrl.isNotEmpty()) {
            ktpImageView.visibility = View.VISIBLE
            Glide.with(this)
                .load(ktpUrl)
                .into(ktpImageView)

            // Make the image clickable
            ktpImageView.setOnClickListener {
                val intent = Intent(this, FullScreenImageActivity::class.java).apply {
                    putExtra("IMAGE_URL", ktpUrl)
                }
                startActivity(intent)
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Verifikasi Masjid")
            .setView(dialogView)
            .setPositiveButton("Verifikasi") { _, _ ->
                val namaMasjid = masjidData["nama"] as? String ?: ""
                verifyMasjid(namaMasjid)
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun verifyMasjid(namaMasjid: String) {
        firestore.collection("user")
            .whereEqualTo("nama", namaMasjid)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents.first()
                    val masjidId = doc.id
                    firestore.collection("user")
                        .document(masjidId)
                        .update("verified", true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Masjid berhasil diverifikasi", Toast.LENGTH_SHORT)
                                .show()
                            fetchMasjidData() // Ambil ulang data setelah berhasil diverifikasi
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Gagal memverifikasi masjid: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(this, "Masjid tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mencari masjid: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}