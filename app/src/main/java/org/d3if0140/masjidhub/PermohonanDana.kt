package org.d3if0140.masjidhub.model

data class PermohonanDana(
    var id: String = "",
    var jumlah: Double = 0.0,
    var alasan: String = "",
    var tanggal: String = "",
    var ktpUrl: String = "",
    var status: String = "pending", // Status could be 'pending' or 'approved'
    var email: String = "",
    var nama: String = "" // Added the nama property
)
