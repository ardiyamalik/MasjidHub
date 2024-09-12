package org.d3if0140.masjidhub.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.model.Notification

class NotificationAdapter(private var notifications: MutableList<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val messageTextView: TextView = itemView.findViewById(R.id.textViewMessage)
        private val timestampTextView: TextView = itemView.findViewById(R.id.textViewTimestamp)
        private val deleteButton: Button = itemView.findViewById(R.id.buttonDelete)

        fun bind(notification: Notification) {
            titleTextView.text = notification.title
            messageTextView.text = notification.message
            val timestampStr = "Timestamp: ${notification.timestamp}"
            timestampTextView.text = timestampStr

            deleteButton.setOnClickListener {
                deleteNotification(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    fun updateList(newList: List<Notification>) {
        notifications.clear()
        notifications.addAll(newList)
        notifyDataSetChanged()
    }

    private fun deleteNotification(position: Int) {
        val notification = notifications[position]
        val db = FirebaseFirestore.getInstance()
        db.collection("notifikasi").document(notification.id)
            .delete()
            .addOnSuccessListener {
                notifications.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener { e ->
                // Handle the error
                e.printStackTrace()
            }
    }
}
