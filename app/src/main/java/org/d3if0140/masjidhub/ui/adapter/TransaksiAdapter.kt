package org.d3if0140.masjidhub.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.d3if0140.masjidhub.databinding.ItemHarianBinding
import org.d3if0140.masjidhub.model.TransaksiHarian

class TransaksiAdapter(private var data: List<TransaksiHarian>) : RecyclerView.Adapter<TransaksiAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemHarianBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHarianBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaksiHarian = data[position]
        holder.binding.tvDate.text = transaksiHarian.tanggal
        holder.binding.incomeHari.text = "Rp${transaksiHarian.totalIncome}"
        holder.binding.expenseHari.text = "Rp${transaksiHarian.totalExpense}"

        Log.d("HarianAdapter", "Binding data: ${transaksiHarian.tanggal} - Income: Rp${transaksiHarian.totalIncome}, Expense: Rp${transaksiHarian.totalExpense}")
    }

    override fun getItemCount() = data.size

    fun updateData(newData: List<TransaksiHarian>) {
        data = newData
        notifyDataSetChanged()
    }
}

