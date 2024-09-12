package org.d3if0140.masjidhub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.d3if0140.masjidhub.databinding.ItemBulananBinding
import org.d3if0140.masjidhub.model.TransaksiBulanan

class TransaksiBulananAdapter(private var data: List<TransaksiBulanan>) :
    RecyclerView.Adapter<TransaksiBulananAdapter.BulananViewHolder>() {

    inner class BulananViewHolder(private val binding: ItemBulananBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaksi: TransaksiBulanan) {
            binding.textViewBulan.text = transaksi.bulan
            binding.incomeBulan.text = "Rp${transaksi.income}"
            binding.expenseBulan.text = "Rp${transaksi.expense}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BulananViewHolder {
        val binding = ItemBulananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BulananViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BulananViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<TransaksiBulanan>) {
        data = newData
        notifyDataSetChanged()
    }
}
