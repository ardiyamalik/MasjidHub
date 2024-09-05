package org.d3if0140.masjidhub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.model.Infaq
import org.d3if0140.masjidhub.model.KasMingguan

class KasMingguanAdapter() : RecyclerView.Adapter<KasMingguanAdapter.KasViewHolder>() {

    private var kasMingguanList = listOf<KasMingguan>()

    fun submitList(list: List<KasMingguan>) {
        kasMingguanList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kas_mingguan, parent, false)
        return KasViewHolder(view)
    }

    override fun onBindViewHolder(holder: KasViewHolder, position: Int) {
        val kas = kasMingguanList[position]
        holder.bind(kas)
    }

    override fun getItemCount() = kasMingguanList.size

    class KasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTanggalBayar: TextView = itemView.findViewById(R.id.textTanggalBayar)
        private val textViewJumlahKas: TextView = itemView.findViewById(R.id.textViewJumlahKas)
        private val textViewUserEmail: TextView = itemView.findViewById(R.id.textViewUserEmail)
        private val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)

        fun bind(KasMingguan: KasMingguan) {
            textViewJumlahKas.text = "Rp ${KasMingguan.jumlah}"
            textViewUserEmail.text = KasMingguan.email
            textTanggalBayar.text = KasMingguan.tanggal
            textViewStatus.text = KasMingguan.status
        }
    }
}
