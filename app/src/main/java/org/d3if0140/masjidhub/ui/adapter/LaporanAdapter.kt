package org.d3if0140.masjidhub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.d3if0140.masjidhub.databinding.ItemLaporanBinding
import org.d3if0140.masjidhub.model.LaporanKeuangan

class LaporanAdapter(private val laporanList: List<LaporanKeuangan>) :
    RecyclerView.Adapter<LaporanAdapter.LaporanViewHolder>() {

    inner class LaporanViewHolder(val binding: ItemLaporanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(laporan: LaporanKeuangan) {
            binding.textViewTanggalLaporan.text = laporan.tanggalLaporan
            binding.textViewJumlahInfaq.text = "Infaq: Rp. ${laporan.jumlahInfaq}"
            binding.textViewJumlahKas.text = "Kas: Rp. ${laporan.jumlahKas}"
            binding.textViewJumlahOprasional.text = "Oprasional: Rp. ${laporan.oprasionalMasjid}"
            binding.textViewJumlahRenov.text = "Renovasi: Rp. ${laporan.renovasi}"
            binding.textViewJumlahKegiatanSosial.text = "Kegiatan Sosial: Rp. ${laporan.kegiatanSosial}"
            binding.textViewJumlahGaji.text = "Gaji Pengurus: ${laporan.gajiPengurus}"
            // Bind other fields here
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaporanViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLaporanBinding.inflate(inflater, parent, false)
        return LaporanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LaporanViewHolder, position: Int) {
        holder.bind(laporanList[position])
    }

    override fun getItemCount(): Int = laporanList.size
}
