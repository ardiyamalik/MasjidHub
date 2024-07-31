package org.d3if0140.masjidhub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.d3if0140.masjidhub.databinding.ItemMasjidBinding

class MasjidAdminAdapter(
    private val masjidList: List<Masjid>,
    private val onItemClick: (Masjid) -> Unit
) : RecyclerView.Adapter<MasjidAdminAdapter.MasjidViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasjidViewHolder {
        val binding = ItemMasjidBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MasjidViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MasjidViewHolder, position: Int) {
        val masjid = masjidList[position]
        holder.bind(masjid, onItemClick)
    }

    override fun getItemCount(): Int = masjidList.size

    class MasjidViewHolder(private val binding: ItemMasjidBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(masjid: Masjid, onItemClick: (Masjid) -> Unit) {
            binding.userNameTextView.text = masjid.nama
            binding.userAlamatTextView.text = masjid.alamat
            Glide.with(binding.root.context)
                .load(masjid.imageUrl)
                .placeholder(R.drawable.baseline_person_black)
                .into(binding.userImageView)
            binding.root.setOnClickListener {
                onItemClick(masjid)
            }
        }
    }
}
