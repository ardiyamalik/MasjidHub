package org.d3if0140.masjidhub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.d3if0140.masjidhub.databinding.ItemInfaqBinding
import org.d3if0140.masjidhub.model.Infaq

class InfaqAdapter(private val onApproveClick: (Infaq) -> Unit) :
    ListAdapter<Infaq, InfaqAdapter.InfaqViewHolder>(InfaqDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfaqViewHolder {
        val binding = ItemInfaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InfaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InfaqViewHolder, position: Int) {
        holder.bind(getItem(position), onApproveClick)
    }

    class InfaqViewHolder(private val binding: ItemInfaqBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(infaq: Infaq, onApproveClick: (Infaq) -> Unit) {
            binding.textViewJumlahInfaq.text = "Rp ${infaq.jumlahInfaq}"
            binding.textViewMetodePembayaran.text = infaq.metodePembayaran
            binding.textViewUserEmail.text = infaq.userEmail

            binding.buttonApprove.setOnClickListener {
                onApproveClick(infaq)
            }
        }
    }

    class InfaqDiffCallback : DiffUtil.ItemCallback<Infaq>() {
        override fun areItemsTheSame(oldItem: Infaq, newItem: Infaq): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Infaq, newItem: Infaq): Boolean {
            return oldItem == newItem
        }
    }
}
