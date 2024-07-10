package org.d3if0140.masjidhub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
        db.collection("notifikasi")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                val notifications = result.map { document ->
                    val id = document.id
                    val title = document.getString("title") ?: "No Title"
                    val message = document.getString("message") ?: "No Message"
                    val timestamp = document.getLong("timestamp") ?: 0L
                    Notification(id, title, message, timestamp)
                }
                adapter.updateList(notifications)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                exception.printStackTrace()
            }
    }
}
