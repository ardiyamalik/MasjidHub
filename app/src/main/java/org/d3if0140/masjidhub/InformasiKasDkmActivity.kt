package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityInformasiKasDkmBinding
import org.d3if0140.masjidhub.databinding.ItemDataKasBinding
import java.text.NumberFormat
import java.util.Locale

data class Transaksi(val tanggal: String, val jumlah: Long)

class InformasiKasDkmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInformasiKasDkmBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val transaksiList = mutableListOf<Transaksi>()
    private var totalKas: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformasiKasDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, ProfilDkmActivity::class.java))
        }

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = TransaksiAdapter(transaksiList)
        binding.recyclerView.adapter = adapter

        // Dapatkan ID pengguna yang saat ini login
        val currentUserId = mAuth.currentUser?.uid

        // Ambil data transaksi dari Firestore
        if (currentUserId != null) {
            firestore.collection("transaksi_keuangan")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("tipe", "kas")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener { documents ->
                    totalKas = 0
                    for (document in documents) {
                        val tanggal = document.getString("tanggal") ?: ""
                        val jumlah = document.getLong("jumlah") ?: 0
                        transaksiList.add(Transaksi(tanggal, jumlah))
                        totalKas += jumlah
                    }
                    adapter.notifyDataSetChanged()

                    // Format totalKas ke dalam format mata uang
                    val formattedTotal = NumberFormat.getNumberInstance(Locale("id", "ID")).format(totalKas)

                    // Tampilkan total kas
                    binding.textTotalKas.text = "Total Kas: $formattedTotal"
                }
                .addOnFailureListener { exception ->
                    // Handle error
                    exception.printStackTrace()
                }
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
