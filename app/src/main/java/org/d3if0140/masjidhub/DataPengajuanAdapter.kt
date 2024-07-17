package org.d3if0140.masjidhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DataPengajuanAdapter(private val dataList: List<DataPengajuan>) : RecyclerView.Adapter<DataPengajuanAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_pengajuan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userEmailTextView: TextView = itemView.findViewById(R.id.user_email_text_view)
        private val jumlahPengajuanTextView: TextView = itemView.findViewById(R.id.jumlah_pengajuan_text_view)

        fun bind(data: DataPengajuan) {
            userEmailTextView.text = "User Email: ${data.userEmail}"
            jumlahPengajuanTextView.text = "Jumlah Kas: ${data.jumlah}" // Sesuaikan dengan atribut DataKas
        }
    }
}