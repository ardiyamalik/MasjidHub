package org.d3if0140.masjidhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

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
    }

    override fun getItemCount(): Int = postList.size

    class DkmPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val postImageView: ImageView = itemView.findViewById(R.id.postImageView)
        val userProfileImageView: ImageView = itemView.findViewById(R.id.profileImageDkm)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }
}
