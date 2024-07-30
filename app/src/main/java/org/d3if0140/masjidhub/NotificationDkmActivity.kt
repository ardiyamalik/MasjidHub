package org.d3if0140.masjidhub

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.d3if0140.masjidhub.databinding.ActivityNotificationDkmBinding

class NotificationDkmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationDkmBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: NotificationDkmAdapter
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationDkmAdapter(mutableListOf())
        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewNotifications.adapter = adapter
    }

    private fun loadNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w("NotificationDkmActivity", "User ID tidak tersedia")
            return
        }

        db.collection("notifikasi_pengurus_dkm")
            .whereEqualTo("userId", userId) // Filter notifikasi berdasarkan ID pengguna
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // Urutkan dari yang terbaru
            .get()
            .addOnSuccessListener { result ->
                Log.d("NotificationDkmActivity", "Notifikasi berhasil diambil. Jumlah: ${result.size()}")

                val notifications = result.map { document ->
                    val id = document.id
                    val title = document.getString("title") ?: "No Title"
                    val message = document.getString("message") ?: "No Message"
                    val timestamp = document.getLong("timestamp") ?: 0L

                    // Logging data notifikasi
                    Log.d("NotificationDkmActivity", "ID: $id, Title: $title, Message: $message, Timestamp: $timestamp")

                    Notification(id, title, message, timestamp)
                }
                adapter.updateList(notifications)
            }
            .addOnFailureListener { exception ->
                Log.e("NotificationDkmActivity", "Gagal mengambil notifikasi", exception)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }
}
