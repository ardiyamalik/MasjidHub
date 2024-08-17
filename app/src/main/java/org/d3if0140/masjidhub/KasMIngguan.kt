package org.d3if0140.masjidhub.model

data class KasMingguan(
    var id: String = "",
    val userId: String = "",
    val email: String = "",
    val nominal: Double = 50000.0,
    val tanggal:String = "",
    val status: String = "pending",
    val buktiPembayaranUrl: String = "",
    val metode: String = ""
)
