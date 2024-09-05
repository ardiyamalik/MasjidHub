package org.d3if0140.masjidhub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.model.Infaq

class InfaqAdapter() : RecyclerView.Adapter<InfaqAdapter.InfaqViewHolder>() {

    private var infaqList = listOf<Infaq>()

    fun submitList(list: List<Infaq>) {
        infaqList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfaqViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_infaq, parent, false)
        return InfaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: InfaqViewHolder, position: Int) {
        val infaq = infaqList[position]
        holder.bind(infaq)
    }

    override fun getItemCount() = infaqList.size

    class InfaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTanggalBayar: TextView = itemView.findViewById(R.id.textTanggalBayar)
        private val textViewJumlahInfaq: TextView = itemView.findViewById(R.id.textViewJumlahInfaq)
        private val textViewUserEmail: TextView = itemView.findViewById(R.id.textViewUserEmail)
        private val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)

        fun bind(infaq: Infaq) {
            textViewJumlahInfaq.text = "Rp ${infaq.jumlah}"
            textViewUserEmail.text = infaq.email
            textTanggalBayar.text = infaq.tanggal
            textViewStatus.text = infaq.status
        }
    }
}
