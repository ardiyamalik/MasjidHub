package org.d3if0140.masjidhub

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(mutableListOf())
        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewNotifications.adapter = adapter
    }

    private fun loadNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w("NotificationActivity", "User ID tidak tersedia")
            return
        }

        db.collection("notifikasi")
            .whereEqualTo("userId", userId) // Filter notifikasi berdasarkan ID pengguna
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // Urutkan dari yang terbaru
            .get()
            .addOnSuccessListener { result ->
                Log.d("NotificationActivity", "Notifikasi berhasil diambil. Jumlah: ${result.size()}")

                val notifications = result.map { document ->
                    val id = document.id
                    val title = document.getString("title") ?: "No Title"
                    val message = document.getString("message") ?: "No Message"
                    val timestamp = document.getLong("timestamp") ?: 0L

                    // Logging data notifikasi
                    Log.d("NotificationActivity", "ID: $id, Title: $title, Message: $message, Timestamp: $timestamp")

                    Notification(id, title, message, timestamp)
                }
                adapter.updateList(notifications)
            }
            .addOnFailureListener { exception ->
                Log.e("NotificationActivity", "Gagal mengambil notifikasi", exception)
            }
    }
}
