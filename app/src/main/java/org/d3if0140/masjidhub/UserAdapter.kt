package org.d3if0140.masjidhub

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
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.RequestOptions

class UserAdapter(private val userList: List<User>, private val onUserClick: (User) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userNameTextView.text = user.nama
        holder.userAddressTextView.text = user.alamat

        if (user.userImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.userImageUrl)
                .apply(RequestOptions().placeholder(R.drawable.baseline_person_black).error(R.drawable.baseline_person_black))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("UserAdapter", "Error loading user image: ${user.userImageUrl}", e)
                        return false // Return false so that Glide can handle the error placeholder
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false // Return false so that Glide can handle the resource
                    }
                })
                .into(holder.userImageView)
        } else {
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
