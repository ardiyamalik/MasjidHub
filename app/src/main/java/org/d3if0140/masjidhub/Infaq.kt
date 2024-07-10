package org.d3if0140.masjidhub.model

data class Infaq(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val jumlahInfaq: Double = 0.0,
    val metodePembayaran: String = "",
    val buktiPembayaran: String = "", // Tambahkan properti ini
    val status: String = "pending"
)
