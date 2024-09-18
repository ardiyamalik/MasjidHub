package org.d3if0140.masjidhub.model

data class LaporanKeuangan(
    val userId: String = "",
    val tanggalLaporan: String = "",
    val jumlahInfaq: Double = 0.0,
    val jumlahKas: Double = 0.0,
    val jumlahPengeluaran: Double = 0.0,
    val oprasionalMasjid: Double = 0.0,
    val renovasi: Double = 0.0,
    val kegiatanSosial: Double = 0.0,
    val gajiPengurus: Double = 0.0
)

