package org.d3if0140.masjidhub.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.ui.adapter.KasMingguanAdapter
import org.d3if0140.masjidhub.databinding.ActivityKasMingguanBinding
import org.d3if0140.masjidhub.model.KasMingguan

class KasMingguanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKasMingguanBinding
    private val db = FirebaseFirestore.getInstance()
    private val kasMingguanList = MutableLiveData<List<KasMingguan>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKasMingguanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = KasMingguanAdapter() // Menggunakan adapter yang benar
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Observe changes to infaqList and submit to adapter
        kasMingguanList.observe(this){ list ->
            adapter.submitList(list)
        }

        fetchKasData()

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
        }
    }

    private fun fetchKasData() {
        db.collection("transaksi_keuangan")
            .whereEqualTo("tipe", "kas") // Filter tipe infaq
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { document ->
                    document.toObject(KasMingguan::class.java).copy(id = document.id)
                }
                kasMingguanList.value = list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data infaq", Toast.LENGTH_SHORT).show()
            }
    }
}
