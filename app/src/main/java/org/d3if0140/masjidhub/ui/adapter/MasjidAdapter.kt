package org.d3if0140.masjidhub.ui.adapter

// MasjidAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.model.Masjid

class MasjidAdapter(private val masjidList: List<Masjid>) :
    RecyclerView.Adapter<MasjidAdapter.MasjidViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasjidViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_masjid, parent, false)
        return MasjidViewHolder(view)
    }

    override fun onBindViewHolder(holder: MasjidViewHolder, position: Int) {
        val masjid = masjidList[position]
        holder.userNamaTextView.text = masjid.nama
        holder.userAlamatTextView.text = masjid.alamat
        Glide.with(holder.itemView.context)
            .load(masjid.imageUrl)
            .into(holder.userImageView)
    }

    override fun getItemCount() = masjidList.size

    class MasjidViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImageView: ImageView = itemView.findViewById(R.id.userImageView)
        val userNamaTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val userAlamatTextView: TextView = itemView.findViewById(R.id.userAlamatTextView)
    }
}

