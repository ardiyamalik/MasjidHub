package org.d3if0140.masjidhub

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class AdminVerifActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var masjidListView: ListView
    private lateinit var masjidListAdapter: ArrayAdapter<String>
    private val masjidList = mutableListOf<String>()
    private val masjidIdList = mutableListOf<String>()
    private val masjidDataList = mutableListOf<Map<String, Any?>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_verif)

        firestore = FirebaseFirestore.getInstance()
        masjidListView = findViewById(R.id.masjidListView)

        masjidListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, masjidList)
        masjidListView.adapter = masjidListAdapter

        fetchMasjidData()

        masjidListView.setOnItemClickListener { _, _, position, _ ->
            val masjidId = masjidIdList[position]
            val masjidData = masjidDataList[position]
            showVerificationDialog(masjidData)
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
            val namaMasjid = document.getString("namaMasjid") ?: "Tidak diketahui"
            masjidList.add(namaMasjid)
            masjidIdList.add(document.id)
            masjidDataList.add(document.data)
        }
        masjidListAdapter.notifyDataSetChanged()
    }

    private fun showVerificationDialog(masjidData: Map<String, Any?>) {
        val masjidName = masjidData["namaMasjid"] as? String ?: "Tidak diketahui"
        val alamat = masjidData["alamat"] as? String ?: "Alamat tidak tersedia"
        val kodePos = masjidData["kodePos"] as? String ?: "Kode Pos tidak tersedia"
        val teleponMasjid = masjidData["teleponMasjid"] as? String ?: "Telepon tidak tersedia"
        val namaKetua = masjidData["namaKetua"] as? String ?: "Nama Ketua tidak tersedia"
        val teleponKetua = masjidData["teleponKetua"] as? String ?: "Telepon Ketua tidak tersedia"
        val email = masjidData["email"] as? String ?: "Email tidak tersedia"

        val message = """
        Nama Masjid: $masjidName
        Alamat: $alamat
        Kode Pos: $kodePos
        Telepon Masjid: $teleponMasjid
        Nama Ketua: $namaKetua
        Telepon Ketua: $teleponKetua
        Email: $email
        
        Apakah Anda yakin ingin memverifikasi masjid ini?
    """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Verifikasi Masjid")
            .setMessage(message)
            .setPositiveButton("Verifikasi") { _, _ ->
                val masjidId = masjidData["id"] as? String ?: ""
                verifyMasjid(masjidId)
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun verifyMasjid(masjidId: String) {
        firestore.collection("user")
            .document(masjidId)
            .update("verified", true)
            .addOnSuccessListener {
                Toast.makeText(this, "Masjid berhasil diverifikasi", Toast.LENGTH_SHORT).show()
                fetchMasjidData()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memverifikasi masjid: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

