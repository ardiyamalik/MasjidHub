package org.d3if0140.masjidhub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
        listenerRegistration = db.collection("notifikasi_pengurus_dkm")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                val notifications = mutableListOf<Notification>()
                snapshots?.forEach { document ->
                    val id = document.id
                    val title = document.getString("title") ?: "No Title"
                    val message = document.getString("message") ?: "No Message"
                    val timestamp = document.getLong("timestamp") ?: 0L
                    notifications.add(Notification(id, title, message, timestamp))
                }
                adapter.updateList(notifications)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }
}
