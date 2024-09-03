package org.d3if0140.masjidhub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.d3if0140.masjidhub.databinding.ItemMingguanBinding

data class TransaksiMingguan(val tanggal: String, val totalIncome: Long, val totalExpense: Long)
class TransaksiMingguanAdapter(private var data: List<TransaksiMingguan>) : RecyclerView.Adapter<TransaksiMingguanAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemMingguanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMingguanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaksiMingguan = data[position]
        holder.binding.tvWeekLabel.text = transaksiMingguan.tanggal
        holder.binding.incomeMingguan.text = "Rp${transaksiMingguan.totalIncome}"
        holder.binding.expenseMingguan.text = "Rp${transaksiMingguan.totalExpense}"
    }

    override fun getItemCount() = data.size

    fun updateData(newData: List<TransaksiMingguan>) {
        data = newData
        notifyDataSetChanged()
    }
}
