package org.d3if0140.masjidhub

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.model.PermohonanDana

class PermohonanDanaAdapter(private val permohonanDanaList: MutableList<PermohonanDana>) :
    RecyclerView.Adapter<PermohonanDanaAdapter.ViewHolder>() {

    private val permohonanDanaBelumDiapprove = mutableListOf<PermohonanDana>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNama: TextView = itemView.findViewById(R.id.textNama)
        val textEmail: TextView = itemView.findViewById(R.id.textEmail)
        val textJumlah: TextView = itemView.findViewById(R.id.textJumlah)
        val textAlasan: TextView = itemView.findViewById(R.id.textAlasan)
        val textTanggal: TextView = itemView.findViewById(R.id.textTanggal)
        val textKontak: TextView = itemView.findViewById(R.id.textKontak)
        val textLokasi: TextView = itemView.findViewById(R.id.textLokasi)
        val textStatus: TextView = itemView.findViewById(R.id.textStatus)
        val imageViewFotoPendukung: ImageView = itemView.findViewById(R.id.imageViewFotoPendukung)
        val imageViewKTP: ImageView = itemView.findViewById(R.id.imageViewKTP)
        val buttonApprove: Button = itemView.findViewById(R.id.buttonApprove)
        val buttonReject: Button = itemView.findViewById(R.id.buttonReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_permohonan_dana, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val permohonanDana = permohonanDanaBelumDiapprove[position]
        holder.textNama.text = holder.itemView.context.getString(R.string.nama_label, permohonanDana.nama)
        holder.textEmail.text = holder.itemView.context.getString(R.string.email_label, permohonanDana.email)
        holder.textJumlah.text = holder.itemView.context.getString(R.string.jumlah_label, permohonanDana.jumlah)
        holder.textAlasan.text = holder.itemView.context.getString(R.string.alasan_label, permohonanDana.alasan)
        holder.textTanggal.text = holder.itemView.context.getString(R.string.tanggal_label, permohonanDana.tanggal)
        holder.textKontak.text = holder.itemView.context.getString(R.string.kontak_label, permohonanDana.kontak)
        holder.textLokasi.text = holder.itemView.context.getString(R.string.lokasi_label, permohonanDana.lokasi)
        holder.textStatus.text = holder.itemView.context.getString(R.string.status_label, permohonanDana.status)


        // Load and display foto pendukung if available
        if (permohonanDana.fotoPendukungUrl.isNotEmpty()) {
            holder.imageViewFotoPendukung.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(permohonanDana.fotoPendukungUrl)
                .centerCrop()
                .into(holder.imageViewFotoPendukung)
        } else {
            holder.imageViewFotoPendukung.visibility = View.GONE
        }

        // Load and display foto KTP if available
        if (permohonanDana.ktpUrl.isNotEmpty()) {
            holder.imageViewKTP.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(permohonanDana.ktpUrl)
                .centerCrop()
                .into(holder.imageViewKTP)
        } else {
            holder.imageViewKTP.visibility = View.GONE
        }

        holder.buttonApprove.setOnClickListener {
            Log.d("PermohonanDanaAdapter", "Approve button clicked for ${permohonanDana.id}")
            approvePermohonanDana(permohonanDana.id, holder.adapterPosition, permohonanDana.email, permohonanDana.jumlah)
        }

        holder.buttonReject.setOnClickListener {
            Log.d("PermohonanDanaAdapter", "Reject button clicked for ${permohonanDana.id}")
            rejectPermohonanDana(permohonanDana.id, holder.adapterPosition, permohonanDana.email, permohonanDana.jumlah)
        }
    }

    override fun getItemCount(): Int = permohonanDanaBelumDiapprove.size

    private fun approvePermohonanDana(id: String, position: Int, email: String, jumlah: Double) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengajuan_dana").document(id)
            .update("status", "approved")
            .addOnSuccessListener {
                Log.d("PermohonanDanaAdapter", "Pengajuan approved for $id")
                removeItem(position)
                sendApprovalNotification(email, jumlah)
            }
            .addOnFailureListener { e ->
                Log.e("PermohonanDanaAdapter", "Error approving pengajuan: ${e.message}", e)
            }
    }

    private fun rejectPermohonanDana(id: String, position: Int, email: String, jumlah: Double) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengajuan_dana").document(id)
            .update("status", "rejected")
            .addOnSuccessListener {
                Log.d("PermohonanDanaAdapter", "Pengajuan rejected for $id")
                removeItem(position)
                sendRejectionNotification(email, jumlah)
            }
            .addOnFailureListener { e ->
                Log.e("PermohonanDanaAdapter", "Error rejecting pengajuan: ${e.message}", e)
            }
    }

    private fun removeItem(position: Int) {
        permohonanDanaBelumDiapprove.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun sendApprovalNotification(email: String, jumlah: Double) {
        val db = FirebaseFirestore.getInstance()
        val notificationData = hashMapOf(
            "title" to "Pengajuan Dana Disetujui",
            "message" to "Pengajuan dana Anda sebesar Rp $jumlah telah disetujui.",
            "email" to email,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("notifikasi_pengurus_dkm")
            .add(notificationData)
            .addOnSuccessListener {
                Log.d("PermohonanDanaAdapter", "Approval notification sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("PermohonanDanaAdapter", "Error sending approval notification: ${e.message}", e)
            }
    }

    private fun sendRejectionNotification(email: String, jumlah: Double) {
        val db = FirebaseFirestore.getInstance()
        val notificationData = hashMapOf(
            "title" to "Pengajuan Dana Ditolak",
            "message" to "Pengajuan dana Anda sebesar Rp $jumlah telah ditolak. Silahkan cek WhatsApp untuk informasi lebih lanjut",
            "email" to email,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("notifikasi_pengurus_dkm")
            .add(notificationData)
            .addOnSuccessListener {
                Log.d("PermohonanDanaAdapter", "Rejection notification sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("PermohonanDanaAdapter", "Error sending rejection notification: ${e.message}", e)
            }
    }

    fun updateList(newList: List<PermohonanDana>) {
        permohonanDanaBelumDiapprove.clear()
        permohonanDanaBelumDiapprove.addAll(newList.filter { it.status != "approved" && it.status != "rejected" })
        notifyDataSetChanged()
    }

}
