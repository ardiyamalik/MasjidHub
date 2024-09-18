package org.d3if0140.masjidhub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.d3if0140.masjidhub.databinding.CarouselItemBinding

class CarouselAdapter(private val imageUrls: List<String>) : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    var onItemClick: ((String) -> Unit)? = null

    class ViewHolder(private val binding: CarouselItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUrl: String, onItemClick: ((String) -> Unit)?) {
            // Load image using Glide
            Glide.with(binding.imageView.context)
                .load(imageUrl)
                .into(binding.imageView)

            binding.root.setOnClickListener {
                onItemClick?.invoke(imageUrl)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CarouselItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageUrls[position], onItemClick)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }
}

