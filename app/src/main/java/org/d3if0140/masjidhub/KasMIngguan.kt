package org.d3if0140.masjidhub.model

data class KasMingguan(
    var id: String = "",
    val userId: String = "",
    var email: String = "",
    var nominal: Double = 50000.0,
    var tanggal:String = "",
    var status: String = "pending",
    var buktiPembayaranUrl: String = "",
    var metode: String = ""
)
