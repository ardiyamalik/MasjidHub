package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.adapter.InfaqAdapter
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

        val adapter = InfaqAdapter(
            onApproveClick = { infaq -> approveInfaq(infaq) },
            onRejectClick = { infaq -> rejectInfaq(infaq) }
        )
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
        db.collection("infaq_masjid")
            .whereEqualTo("status", "pending")
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

    private fun approveInfaq(infaq: Infaq) {
        db.collection("infaq_masjid").document(infaq.id)
            .update("status", "approved")
            .addOnSuccessListener {
                fetchInfaqData() // Refresh data
                sendNotification(infaq, "Infaq telah disetujui")
                Toast.makeText(this, "Infaq telah disetujui", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyetujui infaq", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectInfaq(infaq: Infaq) {
        db.collection("infaq_masjid").document(infaq.id)
            .delete()
            .addOnSuccessListener {
                fetchInfaqData() // Refresh data
                sendNotification(infaq, "Infaq telah ditolak")
                Toast.makeText(this, "Infaq telah ditolak", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menolak infaq", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendNotification(infaq: Infaq, message: String) {
        val notificationData = hashMapOf(
            "title" to "Infaq Diterima",
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "userId" to infaq.userId
        )

        db.collection("notifikasi")
            .add(notificationData)
            .addOnSuccessListener {
                // Notifikasi berhasil disimpan
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
