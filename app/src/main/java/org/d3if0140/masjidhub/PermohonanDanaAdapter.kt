package org.d3if0140.masjidhub

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.model.PermohonanDana

class PermohonanDanaAdapter(private val permohonanDanaList: List<PermohonanDana>) :
    RecyclerView.Adapter<PermohonanDanaAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNama: TextView = itemView.findViewById(R.id.textNama)
        val textEmail: TextView = itemView.findViewById(R.id.textEmail)
        val textJumlah: TextView = itemView.findViewById(R.id.textJumlah)
        val textAlasan: TextView = itemView.findViewById(R.id.textAlasan)
        val textTanggal: TextView = itemView.findViewById(R.id.textTanggal)
        val textStatus: TextView = itemView.findViewById(R.id.textStatus)
        val buttonApprove: Button = itemView.findViewById(R.id.buttonApprove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_permohonan_dana, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val permohonanDana = permohonanDanaList[position]
        holder.textNama.text = "Nama: ${permohonanDana.nama}"
        holder.textEmail.text = "Email: ${permohonanDana.email}"
        holder.textJumlah.text = "Jumlah: Rp ${permohonanDana.jumlah}"
        holder.textAlasan.text = "Alasan: ${permohonanDana.alasan}"
        holder.textTanggal.text = "Tanggal: ${permohonanDana.tanggal}"
        holder.textStatus.text = "Status: ${permohonanDana.status}"

        holder.buttonApprove.setOnClickListener {
            Log.d("PermohonanDanaAdapter", "Approve button clicked for ${permohonanDana.id}")
            approvePermohonanDana(permohonanDana.id)
        }
    }

    override fun getItemCount(): Int = permohonanDanaList.size

    private fun approvePermohonanDana(id: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengajuan_dana").document(id)
            .update("status", "approved")
            .addOnSuccessListener {
                Log.d("PermohonanDanaAdapter", "Pengajuan approved for $id")
                notifyItemChanged(permohonanDanaList.indexOfFirst { it.id == id })
                sendApprovalNotification(id)
            }
            .addOnFailureListener { e ->
                Log.e("PermohonanDanaAdapter", "Error approving pengajuan: ${e.message}", e)
            }
    }

    private fun sendApprovalNotification(id: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengajuan_dana").document(id).get()
            .addOnSuccessListener { document ->
                val userEmail = document.getString("userEmail") ?: return@addOnSuccessListener
                val jumlah = document.getDouble("jumlah") ?: return@addOnSuccessListener

                val notificationData = hashMapOf(
                    "title" to "Pengajuan Dana Disetujui",
                    "message" to "Pengajuan dana sebesar Rp $jumlah oleh $userEmail telah disetujui.",
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("notifikasi_pengurus_dkm")
                    .add(notificationData)
                    .addOnSuccessListener {
                        Log.d("PermohonanDanaAdapter", "Approval notification sent for $id")
                    }
                    .addOnFailureListener { e ->
                        Log.e("PermohonanDanaAdapter", "Error sending notification: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("PermohonanDanaAdapter", "Error getting document: ${e.message}", e)
            }
    }
}
