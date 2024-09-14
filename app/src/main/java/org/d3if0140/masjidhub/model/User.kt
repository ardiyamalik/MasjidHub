package org.d3if0140.masjidhub.model

data class User(
    val userId: String = "",
    val nama: String = "",
    val alamat: String = "",
    val imageUrl: String = "",
    val role: String = "",
    val longitude: Double = 0.0,
    val latitude: Double = 0.0
)

