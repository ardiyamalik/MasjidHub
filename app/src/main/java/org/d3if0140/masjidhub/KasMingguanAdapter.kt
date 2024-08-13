package org.d3if0140.masjidhub

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.model.KasMingguan

class KasMingguanAdapter(
    private val kasMingguanList: MutableList<KasMingguan>,
    private val context: Context
) : RecyclerView.Adapter<KasMingguanAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNama: TextView = itemView.findViewById(R.id.textNama)
        val textTanggalBayar: TextView = itemView.findViewById(R.id.textTanggalBayar)
        val textMetodePembayaran: TextView = itemView.findViewById(R.id.textMetodePembayaran)
        val imageViewBuktiPembayaran: ImageView = itemView.findViewById(R.id.imageViewBuktiPembayaran)
        val buttonApprove: Button = itemView.findViewById(R.id.buttonApprove)
        val buttonReject: Button = itemView.findViewById(R.id.buttonReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kas_mingguan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kasMingguan = kasMingguanList[position]
        holder.textNama.text = context.getString(R.string.emaildkm, kasMingguan.email)
        holder.textTanggalBayar.text = context.getString(R.string.tanggal_bayar, kasMingguan.tanggal)
        holder.textMetodePembayaran.text = context.getString(R.string.metode_pembayaran, kasMingguan.metode)
        Glide.with(holder.itemView.context)
            .load(kasMingguan.buktiPembayaranUrl)
            .into(holder.imageViewBuktiPembayaran)

        holder.buttonApprove.setOnClickListener {
            updateKasMingguanStatus(kasMingguan.id, "approved", holder.adapterPosition)
        }

        holder.buttonReject.setOnClickListener {
            updateKasMingguanStatus(kasMingguan.id, "rejected", holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = kasMingguanList.size

    private fun updateKasMingguanStatus(id: String, status: String, position: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("kas_mingguan").document(id)
            .update("status", status)
            .addOnSuccessListener {
                removeItem(position)
                sendNotification(status, id)
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    private fun sendNotification(status: String, id: String) {
        val db = FirebaseFirestore.getInstance()
        val notification = hashMapOf(
            "title" to "kas telah diterima",
            "message" to "Kas mingguan dengan ID $id telah $status",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("notifikasi_pengurus_dkm")
            .add(notification)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    private fun removeItem(position: Int) {
        kasMingguanList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
}

