package org.d3if0140.masjidhub.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.ui.adapter.InfaqAdapter
import org.d3if0140.masjidhub.databinding.ActivityAdminInfaqBinding
import org.d3if0140.masjidhub.model.Infaq

class AdminInfaqActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminInfaqBinding
    private val db = FirebaseFirestore.getInstance()
    private val infaqList = MutableLiveData<List<Infaq>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminInfaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = InfaqAdapter() // Hilangkan onApproveClick dan onRejectClick
        binding.recyclerViewInfaq.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewInfaq.adapter = adapter

        // Observe changes to infaqList and submit to adapter
        infaqList.observe(this) { list ->
            adapter.submitList(list)
        }

        fetchInfaqData()

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
        }
    }

    private fun fetchInfaqData() {
        db.collection("transaksi_keuangan")
            .whereEqualTo("tipe", "infaq") // Filter tipe infaq
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { document ->
                    document.toObject(Infaq::class.java).copy(id = document.id)
                }
                infaqList.value = list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data infaq", Toast.LENGTH_SHORT).show()
            }
    }
}
