package org.d3if0140.masjidhub.model

data class Infaq(
    var id: String = "",
    var jumlahInfaq: Double = 0.0,
    var metodePembayaran: String = "",
    var buktiPembayaran: String = "", // Assuming this is a URL or URI to the image
    var status: String = "pending", // Status could be 'pending' or 'approved'
    var userEmail: String = ""
)
