package org.d3if0140.masjidhub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.d3if0140.masjidhub.databinding.ItemTahunanBinding
import org.d3if0140.masjidhub.model.TransaksiTahunan

class TransaksiTahunanAdapter(private var data: List<TransaksiTahunan>) :
    RecyclerView.Adapter<TransaksiTahunanAdapter.TahunanViewHolder>() {

    inner class TahunanViewHolder(private val binding: ItemTahunanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaksi: TransaksiTahunan) {
            binding.textViewTahun.text = transaksi.tahun
            binding.incomeTahun.text = "Rp${transaksi.income}"
            binding.expenseTahun.text = "Rp${transaksi.expense}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TahunanViewHolder {
        val binding = ItemTahunanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TahunanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TahunanViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<TransaksiTahunan>) {
        data = newData
        notifyDataSetChanged()
    }
}
