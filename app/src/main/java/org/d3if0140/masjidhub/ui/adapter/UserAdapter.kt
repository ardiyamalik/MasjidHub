package org.d3if0140.masjidhub.ui.adapter

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.model.User

class UserAdapter(private val userList: List<User>, private val onUserClick: (User) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userNameTextView.text = user.nama
        holder.userAddressTextView.text = user.alamat

        Log.d("UserAdapter", "Loading image for user: ${user.nama}, URL: ${user.imageUrl}")

        if (!user.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.imageUrl)
                .apply(RequestOptions()
                    .placeholder(R.drawable.baseline_person_black)
                    .error(R.drawable.baseline_person_black))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("UserAdapter", "Error loading user image from URL: ${user.imageUrl}", e)
                        return false // Return false so Glide can handle the error placeholder
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("UserAdapter", "Image loaded successfully for user: ${user.nama}")
                        return false // Return false so Glide can handle the resource
                    }
                })
                .into(holder.userImageView)
        } else {
            Log.d("UserAdapter", "Empty image URL for user: ${user.nama}")
            holder.userImageView.setImageResource(R.drawable.baseline_person_black)
        }

        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val userAddressTextView: TextView = itemView.findViewById(R.id.userAlamatTextView)
        val userImageView: ImageView = itemView.findViewById(R.id.userImageView)
    }
}
