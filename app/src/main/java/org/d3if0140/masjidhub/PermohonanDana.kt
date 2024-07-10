package org.d3if0140.masjidhub.model

data class PermohonanDana(
    var id: String = "",         // ID dari dokumen dalam Firestore
    var jumlah: Double = 0.0,    // Jumlah dana yang diajukan
    var alasan: String = "",     // Alasan pengajuan dana
    var tanggal: String = "",    // Tanggal pengajuan
    var ktpUrl: String = "",     // URL foto KTP
    var fotoPendukungUrl: String = "",  // URL foto pendukung (jika ada)
    var status: String = "pending",     // Status pengajuan ('pending' atau 'approved')
    var email: String = "",      // Email pengaju
    var nama: String = "",       // Nama pengaju
    var kontak: String = "",     // Nomor kontak untuk approval
    var lokasi: String = ""      // Lokasi perbaikan atau penggunaan dana
)
