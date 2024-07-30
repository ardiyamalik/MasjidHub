package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.d3if0140.masjidhub.adapter.InfaqAdapter
import org.d3if0140.masjidhub.databinding.ActivityAdminInfaqBinding
import org.d3if0140.masjidhub.model.Infaq
import org.d3if0140.masjidhub.viewmodel.AdminInfaqViewModel
import com.google.firebase.firestore.FirebaseFirestore

class AdminInfaqActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminInfaqBinding
    private val viewModel: AdminInfaqViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()

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

        viewModel.infaqList.observe(this) { infaqList ->
            adapter.submitList(infaqList)
        }

        binding.backButton.setOnClickListener{
            startActivity(Intent(this, AdminDashboard::class.java))
        }
    }

    private fun approveInfaq(infaq: Infaq) {
        viewModel.approveInfaq(infaq) { success ->
            if (success) {
                sendNotification(infaq, "Infaq telah disetujui")
                Toast.makeText(this, "Infaq telah disetujui", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menyetujui infaq", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rejectInfaq(infaq: Infaq) {
        viewModel.rejectInfaq(infaq) { success ->
            if (success) {
                sendNotification(infaq, "Infaq telah ditolak")
                Toast.makeText(this, "Infaq telah ditolak", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menolak infaq", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendNotification(infaq: Infaq, message: String) {
        val notificationData = hashMapOf(
            "title" to "Infaq Disetujui",
            "message" to "Infaq sebesar Rp ${infaq.jumlahInfaq} telah disetujui.",
            "timestamp" to System.currentTimeMillis(),
            "userId" to infaq.userId
        )

        db.collection("notifikasi")
            .add(notificationData)
            .addOnSuccessListener {
                // Notifikasi berhasil disimpan
            }
            .addOnFailureListener { e ->
                // Gagal menyimpan notifikasi
                e.printStackTrace()
            }
    }
}
