package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
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
    private lateinit var masjidListAdapter: ArrayAdapter<String>
    private val masjidList = mutableListOf<String>()
    private val masjidIdList = mutableListOf<String>()
    private val masjidDataList = mutableListOf<Map<String, Any?>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminVerifBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        masjidListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, masjidList)
        binding.masjidListView.adapter = masjidListAdapter

        fetchMasjidData()

        binding.masjidListView.setOnItemClickListener { _, _, position, _ ->
            val masjidData = masjidDataList[position]
            showVerificationDialog(masjidData)
        }

        binding.backButton.setOnClickListener{
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
        masjidIdList.clear()
        masjidDataList.clear()
        for (document in result) {
            val masjidId = document.id
            val masjidData = document.data
            val namaMasjid = masjidData["nama"] as? String ?: "Tidak diketahui"
            masjidList.add(namaMasjid)
            masjidIdList.add(masjidId)
            masjidDataList.add(masjidData)
        }
        masjidListAdapter.notifyDataSetChanged()
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

        val message = """
            Nama : $masjidName
            Alamat: $alamat
            Kode Pos: $kodePos
            Telepon Masjid: $teleponMasjid
            Nama Ketua: $namaKetua
            Telepon Ketua: $teleponKetua
            Email: $email
            
            Apakah Anda yakin ingin memverifikasi masjid ini?
            
            Informasi Pengurus DKM:
            Nama: ${masjidData["namaKetua"]}
            Alamat: ${masjidData["alamat"]}
            Telepon: ${masjidData["teleponKetua"]}
        """.trimIndent()

        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)

            // Add a ScrollView to contain the message TextView
            val scrollView = ScrollView(context).apply {
                addView(TextView(context).apply {
                    text = message
                    setPadding(0, 0, 0, 16)
                })
            }
            addView(scrollView)

            // Add an ImageView to display the KTP image
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    400
                ).apply {
                    setMargins(0, 16, 0, 16)
                }
                if (ktpUrl.isNotEmpty()) {
                    Glide.with(context)
                        .load(ktpUrl)
                        .into(this)

                    // Make the image clickable
                    setOnClickListener {
                        val intent = Intent(context, FullScreenImageActivity::class.java).apply {
                            putExtra("IMAGE_URL", ktpUrl)
                        }
                        startActivity(intent)
                    }

                }
            }
            addView(imageView)
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
