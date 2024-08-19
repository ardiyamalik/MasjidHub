package org.d3if0140.masjidhub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.d3if0140.masjidhub.databinding.ItemPengurusDkmBinding

data class PengurusDkm(val nama: String, val alamat: String, val imageUrl: String, val userId: String)

class PengurusDkmAdapter(
    private val onItemClick: (PengurusDkm) -> Unit
) : RecyclerView.Adapter<PengurusDkmAdapter.ViewHolder>() {

    private var pengurusDkmList = mutableListOf<PengurusDkm>()

    fun setData(data: List<PengurusDkm>) {
        pengurusDkmList.clear()
        pengurusDkmList.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPengurusDkmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pengurusDkm = pengurusDkmList[position]
        holder.bind(pengurusDkm)
        holder.itemView.setOnClickListener {
            onItemClick(pengurusDkm) // Trigger the onItemClick callback
        }
    }

    override fun getItemCount(): Int {
        return pengurusDkmList.size
    }

    class ViewHolder(private val binding: ItemPengurusDkmBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pengurusDkm: PengurusDkm) {
            binding.textViewNama.text = pengurusDkm.nama
            binding.textViewAlamat.text = pengurusDkm.alamat
            Glide.with(binding.root.context)
                .load(pengurusDkm.imageUrl)
                .placeholder(R.drawable.baseline_person_black)
                .into(binding.imageViewProfile)
        }
    }
}
