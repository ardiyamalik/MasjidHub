package org.d3if0140.masjidhub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import org.d3if0140.masjidhub.databinding.CarouselItemBinding

class CarouselAdapter(private val imageList: List<Int>) : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: CarouselItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageResId: Int) {
            Glide.with(binding.root)
                .load(imageResId)
                .centerCrop()
                .into(binding.imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CarouselItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageResId = imageList[position]
        holder.bind(imageResId)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
