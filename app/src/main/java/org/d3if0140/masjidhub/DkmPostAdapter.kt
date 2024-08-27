package org.d3if0140.masjidhub

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DkmPostAdapter(
    private val postList: MutableList<Post>,
    private val onEditCaption: (Post) -> Unit,
    private val onDelete: (Post) -> Unit
) : RecyclerView.Adapter<DkmPostAdapter.DkmPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DkmPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_dkm, parent, false)
        return DkmPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: DkmPostViewHolder, position: Int) {
        val post = postList[position]
        holder.descriptionTextView.text = post.deskripsi
        holder.usernameTextView.text = post.nama

        // Format timestamp to a readable date string
        val formattedDate = formatTimestamp(post.timestamp)
        holder.timestampTextView.text = formattedDate

        Glide.with(holder.itemView.context)
            .load(post.userImageUrl)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.userProfileImageView)

        Glide.with(holder.itemView.context)
            .load(post.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.postImageView)

        holder.editButton.setOnClickListener {
            onEditCaption(post)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(post)
        }

        // Set click listener for post image
        holder.postImageView.setOnClickListener {
            val intent = Intent(holder.itemView.context, FullScreenImageActivity::class.java).apply {
                putExtra("IMAGE_URL", post.imageUrl)
                putExtra("CAPTION", post.deskripsi)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = postList.size

    class DkmPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val postImageView: ImageView = itemView.findViewById(R.id.postImageView)
        val userProfileImageView: ImageView = itemView.findViewById(R.id.profileImageDkm)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
