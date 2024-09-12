package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityAdminPermohonanBinding
import org.d3if0140.masjidhub.model.PermohonanDana

class AdminPermohonan : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPermohonanBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var permohonanDanaList: MutableList<PermohonanDana>
    private lateinit var adapter: PermohonanDanaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPermohonanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        permohonanDanaList = mutableListOf()
        adapter = PermohonanDanaAdapter(permohonanDanaList)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AdminPermohonan)
            adapter = this@AdminPermohonan.adapter
        }

        fetchPermohonanDana()

        binding.backButton.setOnClickListener {
            val intent = Intent(this, AdminDashboard::class.java)
            startActivity(intent)
        }
    }

    private fun fetchPermohonanDana() {
        db.collection("transaksi_keuangan")
            .whereEqualTo("status", "Pending") // Filter status pending
            .whereEqualTo("tipe", "pengajuan_dana") // Filter tipe pengajuan_dana
            .get()
            .addOnSuccessListener { result ->
                permohonanDanaList.clear()
                for (document in result) {
                    val id = document.id
                    val userId = document.id
                    val nama = document.getString("nama") ?: ""
                    val userEmail = document.getString("userEmail") ?: ""
                    val jumlah = document.getDouble("jumlah") ?: 0.0
                    val alasan = document.getString("alasan") ?: ""
                    val tanggal = document.getString("tanggal") ?: ""
                    val kontak = document.getString("kontak") ?: ""
                    val lokasi = document.getString("lokasi") ?: ""
                    val status = document.getString("status") ?: ""
                    val namaBank = document.getString("namaBank") ?: ""
                    val nomorRekening = document.getString("nomorRekening") ?: ""
                    val namaRekening = document.getString("namaRekening") ?: ""
                    val buktiUrl = document.getString("buktiUrl") ?: ""
                    val ktpUrl = document.getString("ktpUrl") ?: ""

                    val permohonanDana = PermohonanDana(id,userId, nama, userEmail, jumlah, alasan, tanggal, kontak, lokasi, status, namaBank, nomorRekening, namaRekening, buktiUrl, ktpUrl)
                    permohonanDanaList.add(permohonanDana)
                }
                adapter.updateList(permohonanDanaList)
                Log.d("AdminPermohonan", "Fetch permohonan dana success. Total: ${permohonanDanaList.size} items.")
            }
            .addOnFailureListener { exception ->
                Log.e("AdminPermohonan", "Error getting documents: ", exception)
            }
    }
}