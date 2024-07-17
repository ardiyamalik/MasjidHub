package org.d3if0140.masjidhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MasjidAdapter(private val masjidList: List<Masjid>) :
    RecyclerView.Adapter<MasjidAdapter.MasjidViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasjidViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_masjid, parent, false)
        return MasjidViewHolder(view)
    }

    override fun onBindViewHolder(holder: MasjidViewHolder, position: Int) {
        val masjid = masjidList[position]
        holder.namaTextView.text = masjid.nama
        holder.userAlamatTextView.text = masjid.alamat
    }

    override fun getItemCount() = masjidList.size

    class MasjidViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.userImageView)
        val namaTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val userAlamatTextView: TextView = itemView.findViewById(R.id.userAlamatTextView)
    }
}
