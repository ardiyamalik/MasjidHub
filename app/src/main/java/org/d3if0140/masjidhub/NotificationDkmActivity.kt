package org.d3if0140.masjidhub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityNotificationDkmBinding

class NotificationDkmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationDkmBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: NotificationDkmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationDkmAdapter(emptyList())
        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewNotifications.adapter = adapter
    }

    private fun loadNotifications() {
        db.collection("notifikasi_pengurus_dkm")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                val notifications = result.map { document ->
                    val title = document.getString("title") ?: "No Title"
                    val message = document.getString("message") ?: "No Message"
                    val timestamp = document.getLong("timestamp") ?: 0L
                    Notification(title, message, timestamp)
                }
                adapter = NotificationDkmAdapter(notifications)
                binding.recyclerViewNotifications.adapter = adapter
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }
}
