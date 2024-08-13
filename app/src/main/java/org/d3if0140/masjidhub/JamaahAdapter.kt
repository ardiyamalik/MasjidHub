package org.d3if0140.masjidhub


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.d3if0140.masjidhub.databinding.ItemJamaahBinding

class JamaahAdapter(
    private val jamaahList: List<Jamaah>,
    private val onItemClick: (Jamaah) -> Unit
) : RecyclerView.Adapter<JamaahAdapter.JamaahViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JamaahViewHolder {
        val binding = ItemJamaahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JamaahViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JamaahViewHolder, position: Int) {
        val jamaah = jamaahList[position]
        holder.bind(jamaah, onItemClick)
    }

    override fun getItemCount(): Int = jamaahList.size

    class JamaahViewHolder(private val binding: ItemJamaahBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(jamaah: Jamaah, onItemClick: (Jamaah) -> Unit) {
            binding.userNameTextView.text = jamaah.nama
            binding.userDkmTextView.text = jamaah.dkm
            Glide.with(binding.root.context)
                .load(jamaah.imageUrl)
                .placeholder(R.drawable.baseline_person_black)
                .into(binding.userImageView)
            binding.root.setOnClickListener {
                onItemClick(jamaah)
            }
        }
    }
}
