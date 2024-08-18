package org.d3if0140.masjidhub.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.d3if0140.masjidhub.FullScreenImageActivity
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.model.Infaq

class InfaqAdapter(
    private val onApproveClick: (Infaq) -> Unit,
    private val onRejectClick: (Infaq) -> Unit
) : RecyclerView.Adapter<InfaqAdapter.InfaqViewHolder>() {

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
        holder.bind(infaq, onApproveClick, onRejectClick)
    }

    override fun getItemCount() = infaqList.size

    class InfaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTanggalBayar: TextView = itemView.findViewById(R.id.textTanggalBayar)
        private val textViewJumlahInfaq: TextView = itemView.findViewById(R.id.textViewJumlahInfaq)
        private val textViewMetodePembayaran: TextView = itemView.findViewById(R.id.textViewMetodePembayaran)
        private val textViewUserEmail: TextView = itemView.findViewById(R.id.textViewUserEmail)
        private val imageViewBuktiPembayaran: ImageView = itemView.findViewById(R.id.imageViewBuktiPembayaran)
        private val buttonApprove: Button = itemView.findViewById(R.id.buttonApprove)
        private val buttonReject: Button = itemView.findViewById(R.id.buttonReject)

        fun bind(
            infaq: Infaq,
            onApproveClick: (Infaq) -> Unit,
            onRejectClick: (Infaq) -> Unit
        ) {
            textViewJumlahInfaq.text = "Rp ${infaq.jumlahInfaq}"
            textViewMetodePembayaran.text = infaq.metodePembayaran
            textViewUserEmail.text = infaq.userEmail
            textTanggalBayar.text = infaq.tanggal

            Glide.with(itemView.context)
                .load(infaq.buktiPembayaran)
                .into(imageViewBuktiPembayaran)

            buttonApprove.setOnClickListener { onApproveClick(infaq) }
            buttonReject.setOnClickListener { onRejectClick(infaq) }

            imageViewBuktiPembayaran.setOnClickListener {
                val intent = Intent(itemView.context, FullScreenImageActivity::class.java).apply {
                    putExtra("IMAGE_URL", infaq.buktiPembayaran)
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}
