package org.d3if0140.masjidhub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            navigateToVerifikasiMasjidActivity(masjidData)
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
            val masjidData = document.data
            val namaMasjid = masjidData["nama"] as? String ?: "Tidak diketahui"
            val alamat = masjidData["alamat"] as? String ?: "Alamat tidak tersedia"
            masjidList.add(namaMasjid)
            alamatList.add(alamat)
            masjidDataList.add(masjidData)
        }
        masjidListAdapter.notifyDataSetChanged()
    }

    private fun navigateToVerifikasiMasjidActivity(masjidData: Map<String, Any?>) {
        val intent = Intent(this, VerifikasiMasjidActivity::class.java).apply {
            putExtra("NAMA_MASJID", masjidData["nama"] as? String ?: "Tidak diketahui")
            putExtra("ALAMAT", masjidData["alamat"] as? String ?: "Alamat tidak tersedia")
            putExtra("KODE_POS", masjidData["kodePos"] as? String ?: "Kode Pos tidak tersedia")
            putExtra("TELEPON_MASJID", masjidData["teleponMasjid"] as? String ?: "Telepon tidak tersedia")
            putExtra("NAMA_KETUA", masjidData["namaKetua"] as? String ?: "Nama Ketua tidak tersedia")
            putExtra("TELEPON_KETUA", masjidData["teleponKetua"] as? String ?: "Telepon Ketua tidak tersedia")
            putExtra("EMAIL", masjidData["email"] as? String ?: "Email tidak tersedia")
            putExtra("LATITUDE", masjidData["latitude"] as? String ?: "Tidak tersedia")
            putExtra("LONGITUDE", masjidData["longitude"] as? String ?: "Tidak tersedia")
            putExtra("KTP_URL", masjidData["ktpKetuaUrl"] as? String ?: "")
        }
        startActivity(intent)
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
}
