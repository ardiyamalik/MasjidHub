package org.d3if0140.masjidhub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.d3if0140.masjidhub.databinding.ItemLaporanBinding
import org.d3if0140.masjidhub.model.LaporanKeuangan
import java.text.DecimalFormat

class LaporanAdapter(private val laporanList: List<LaporanKeuangan>) :
    RecyclerView.Adapter<LaporanAdapter.LaporanViewHolder>() {

    inner class LaporanViewHolder(val binding: ItemLaporanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val decimalFormat = DecimalFormat("#,###") // Format angka dengan pemisah ribuan

        fun bind(laporan: LaporanKeuangan) {
            // Hitung total pengeluaran
            val totalPengeluaran = (laporan.oprasionalMasjid ?: 0.0) +
                    (laporan.renovasi ?: 0.0) +
                    (laporan.kegiatanSosial ?: 0.0) +
                    (laporan.gajiPengurus ?: 0.0)

            // Hitung total saldo (kas + infaq - pengeluaran)
            val totalSaldo = (laporan.jumlahKas ?: 0.0) + (laporan.jumlahInfaq ?: 0.0) - totalPengeluaran

            // Bind data ke UI
            binding.textViewTanggalLaporan.text = laporan.tanggalLaporan
            binding.textViewJumlahInfaq.text = "Infaq: Rp. ${decimalFormat.format(laporan.jumlahInfaq ?: 0.0)}"
            binding.textViewJumlahKas.text = "Kas: Rp. ${decimalFormat.format(laporan.jumlahKas ?: 0.0)}"
            binding.textViewJumlahOprasional.text = "Oprasional: Rp. ${decimalFormat.format(laporan.oprasionalMasjid ?: 0.0)}"
            binding.textViewJumlahRenov.text = "Renovasi: Rp. ${decimalFormat.format(laporan.renovasi ?: 0.0)}"
            binding.textViewJumlahKegiatanSosial.text = "Kegiatan Sosial: Rp. ${decimalFormat.format(laporan.kegiatanSosial ?: 0.0)}"
            binding.textViewJumlahGaji.text = "Gaji Pengurus: Rp. ${decimalFormat.format(laporan.gajiPengurus ?: 0.0)}"

            // Tampilkan total pengeluaran dan saldo
            binding.textViewTotalPengeluaran.text = "Total Pengeluaran: Rp. ${decimalFormat.format(totalPengeluaran)}"
            binding.textViewTotalSaldo.text = "Total Saldo: Rp. ${decimalFormat.format(totalSaldo)}"
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
