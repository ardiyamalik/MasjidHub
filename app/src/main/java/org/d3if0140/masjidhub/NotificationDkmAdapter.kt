package org.d3if0140.masjidhub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.d3if0140.masjidhub.databinding.ItemNotificationBinding

class NotificationDkmAdapter(private val notifications: List<Notification>) : RecyclerView.Adapter<NotificationDkmAdapter.NotificationDkmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationDkmViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationDkmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationDkmViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    class NotificationDkmViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: Notification) {
            binding.textViewTitle.text = notification.title
            binding.textViewMessage.text = notification.message
            binding.textViewTimestamp.text = notification.timestamp.toString()
        }
    }
}
