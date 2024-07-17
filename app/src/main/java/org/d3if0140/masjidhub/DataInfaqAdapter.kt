package org.d3if0140.masjidhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DataInfaqAdapter(private val dataList: List<DataInfaq>) : RecyclerView.Adapter<DataInfaqAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_infaq, parent, false)
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
        private val jumlahInfaqTextView: TextView = itemView.findViewById(R.id.jumlah_infaq_text_view)

        fun bind(data: DataInfaq) {
            userEmailTextView.text = "User Email: ${data.userEmail}"
            jumlahInfaqTextView.text = "Jumlah Infaq: ${data.jumlahInfaq}"
        }
    }
}

