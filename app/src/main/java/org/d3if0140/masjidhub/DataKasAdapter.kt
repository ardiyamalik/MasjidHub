package org.d3if0140.masjidhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DataKasAdapter(private val dataList: List<DataKas>) : RecyclerView.Adapter<DataKasAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_kas, parent, false)
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
        private val jumlahKasTextView: TextView = itemView.findViewById(R.id.jumlah_kas_text_view)

        fun bind(data: DataKas) {
            userEmailTextView.text = "User Email: ${data.email}"
            jumlahKasTextView.text = "Jumlah Kas: ${data.jumlah}" // Sesuaikan dengan atribut DataKas
        }
    }
}
