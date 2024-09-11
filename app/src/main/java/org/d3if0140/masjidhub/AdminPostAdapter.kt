package org.d3if0140.masjidhub

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminPostAdapter(
    private val postList: MutableList<Post>,
    private val onDelete: (Post) -> Unit
) : RecyclerView.Adapter<AdminPostAdapter.AdminPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_admin, parent, false)
        return AdminPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminPostViewHolder, position: Int) {
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

        // Set click listener for user profile image and username
        val openProfileSearch = View.OnClickListener {
            val intent = Intent(holder.itemView.context, ProfileSearchAdmin::class.java).apply {
                putExtra("USER_ID", post.userId)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.userProfileImageView.setOnClickListener(openProfileSearch)
        holder.usernameTextView.setOnClickListener(openProfileSearch)
    }

    override fun getItemCount(): Int = postList.size

    class AdminPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val postImageView: ImageView = itemView.findViewById(R.id.postImageView)
        val userProfileImageView: ImageView = itemView.findViewById(R.id.profileImageDkm)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
