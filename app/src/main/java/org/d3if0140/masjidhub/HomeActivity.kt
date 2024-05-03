package org.d3if0140.masjidhub

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val DEFAULT_ZOOM = 12f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize SupportMapFragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(map: GoogleMap?) {
        map?.let {
            googleMap = it

            // Pastikan izin lokasi diberikan
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Jika izin lokasi belum diberikan, minta izin
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
                return
            }

            // Aktifkan tombol "My Location"
            googleMap.isMyLocationEnabled = true

            // Tampilkan marker lokasi pengguna saat ini
            showCurrentLocation()

            // Tampilkan marker untuk lokasi masjid terdekat
            showNearbyMosques()
        }
    }

    private fun showCurrentLocation() {
        // Cek izin lokasi lagi
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Dapatkan lokasi pengguna saat ini
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
                } else {
                    Toast.makeText(
                        this,
                        "Tidak dapat menemukan lokasi Anda saat ini",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showNearbyMosques() {
        // Misalkan Anda memiliki data lokasi masjid yang disimpan dalam sebuah list
        val masjidLocations: List<MasjidLocation> = getNearbyMosques()

        // Loop melalui data lokasi masjid dan tambahkan marker untuk setiap lokasi
        for (location in masjidLocations) {
            val masjidLatLng = LatLng(location.latitude, location.longitude)
            val markerOptions = MarkerOptions()
                .position(masjidLatLng)
                .title(location.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // Mengatur ikon marker menjadi biru (opsional)
            googleMap.addMarker(markerOptions)
        }
    }

    // Fungsi ini digunakan untuk mendapatkan data lokasi masjid terdekat
    private fun getNearbyMosques(): List<MasjidLocation> {
        // Di sini Anda bisa mengambil data lokasi masjid terdekat dari sumber data yang tersedia,
        // seperti API publik yang menyediakan data lokasi masjid, atau basis data internal aplikasi Anda.
        // Untuk contoh, kita akan mengembalikan data dummy secara sederhana.
        return listOf(
            MasjidLocation("Masjid Nurul Hikmah", -7.0242844135833415, 107.54263401006874),
            MasjidLocation("Al - Islah", -7.027777046012649, 107.53967285135468),
            MasjidLocation("Masjid Riyadhul Muttaqin", -7.028160382120132, 107.53825664501313)
        )
    }
}

data class MasjidLocation(val name: String, val latitude: Double, val longitude: Double)
