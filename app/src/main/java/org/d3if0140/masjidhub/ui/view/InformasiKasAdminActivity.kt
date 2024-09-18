package org.d3if0140.masjidhub.ui.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityInformasiKasAdminBinding
import org.d3if0140.masjidhub.databinding.ItemDataKasBinding
import org.d3if0140.masjidhub.model.Transaksi

class InformasiKasAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInformasiKasAdminBinding
    private lateinit var firestore: FirebaseFirestore
    private val transaksiList = mutableListOf<Transaksi>()
    private var totalKas: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformasiKasAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firestore
        firestore = FirebaseFirestore.getInstance()

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = TransaksiAdapter(transaksiList)
        binding.recyclerView.adapter = adapter

        // Mendapatkan userId dari Intent
        val visitedUserId = intent.getStringExtra("USER_ID")
        Log.d("InformasiKasSearch", "Visited User ID: $visitedUserId")

        if (visitedUserId != null) {
            firestore.collection("transaksi_keuangan")
                .whereEqualTo("userId", visitedUserId)
                .whereEqualTo("tipe", "kas")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener { documents ->
                    Log.d("InformasiKasSearch", "Documents fetched successfully: ${documents.size()}")

                    totalKas = 0
                    transaksiList.clear()
                    for (document in documents) {
                        val tanggal = document.getString("tanggal") ?: ""
                        val jumlah = document.getLong("jumlah") ?: 0
                        Log.d("InformasiKasSearch", "Document: tanggal = $tanggal, jumlah = $jumlah")
                        transaksiList.add(Transaksi(tanggal, jumlah))
                        totalKas += jumlah
                    }
                    adapter.notifyDataSetChanged()
                    binding.textTotalKas.text = "Total Kas: $totalKas"
                }
                .addOnFailureListener { exception ->
                    Log.e("InformasiKasSearch", "Error fetching documents", exception)
                }
        } else {
            Log.e("InformasiKasSearch", "Visited User ID is null")
        }
    }

    // Adapter untuk RecyclerView
    class TransaksiAdapter(private val transaksiList: List<Transaksi>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<TransaksiAdapter.TransaksiViewHolder>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TransaksiViewHolder {
            // Inflate layout item menggunakan ItemDataKasBinding
            val binding = ItemDataKasBinding.inflate(
                android.view.LayoutInflater.from(parent.context), parent, false
            )
            return TransaksiViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
            val transaksi = transaksiList[position]
            holder.bind(transaksi)
        }

        override fun getItemCount(): Int = transaksiList.size

        class TransaksiViewHolder(private val binding: ItemDataKasBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

            fun bind(transaksi: Transaksi) {
                binding.textTanggal.text = transaksi.tanggal // Menggunakan textTanggal dari ItemDataKasBinding
                binding.jumlahKas.text = transaksi.jumlah.toString() // Menggunakan jumlahKas dari ItemDataKasBinding
            }
        }
    }
}
