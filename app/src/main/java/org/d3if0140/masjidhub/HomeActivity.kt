package org.d3if0140.masjidhub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.Place
import org.d3if0140.masjidhub.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private val DEFAULT_ZOOM = 12f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Places API
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)

        // Initialize SupportMapFragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up carousel
        val imageList = listOf(
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.banner3
        )
        val adapter = CarouselAdapter(imageList)
        binding.viewPager.adapter = adapter

        // Bottom navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> true
                R.id.search_masjid -> {
                    val intent = Intent(this, CariMasjidActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_finance -> {
                    val intent = Intent(this, KeuanganActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_profile -> {
                    val intent = Intent(this, ProfilActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        // Profile image click listener
        binding.profileImageView.setOnClickListener {
            val intent = Intent(this, ProfilActivity::class.java)
            startActivity(intent)
        }

        // Display default profile image based on user's email
        displayDefaultProfileImage()
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

            // Dapatkan lokasi pengguna saat ini
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))

                    // Tampilkan marker untuk lokasi masjid terdekat
                    showNearbyMosques(location)
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

    private fun showNearbyMosques(currentLocation: Location) {
        // Misalkan Anda memiliki data lokasi masjid yang disimpan dalam sebuah list
        val masjidLocations: List<MasjidLocation> = getNearbyMosques()

        // Loop melalui data lokasi masjid dan tambahkan marker untuk setiap lokasi
        for (location in masjidLocations) {
            val masjidLatLng = LatLng(location.latitude, location.longitude)
            val masjidLocation = Location("Masjid")
            masjidLocation.latitude = location.latitude
            masjidLocation.longitude = location.longitude

            // Hitung jarak antara lokasi perangkat dan lokasi masjid
            val distance = currentLocation.distanceTo(masjidLocation)

            // Tambahkan marker hanya untuk lokasi masjid yang berjarak kurang dari 5 km dari lokasi perangkat
            if (distance < 5000) {
                val markerOptions = MarkerOptions()
                    .position(masjidLatLng)
                    .title(location.name)
                    .icon(bitmapDescriptorFromVector(R.drawable.baseline_mosque)) // Mengatur ikon marker menjadi ikon masjid (opsional)
                googleMap.addMarker(markerOptions)
            }
        }
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

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

    private fun displayDefaultProfileImage() {
        // Get current user
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Get user's email address
            val email = user.email
            email?.let {
                // Create avatar based on email address and display it in profile image view
                val avatarDrawable = createAvatar(it)
                binding.profileImageView.setImageDrawable(avatarDrawable)
            }
        }
    }

    private fun createAvatar(email: String): Drawable {
        // Get color from email hashcode
        val color = getColorFromEmail(email)

        // Create blank bitmap for avatar
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw colored background
        val paint = Paint()
        paint.color = color
        canvas.drawCircle(50f, 50f, 50f, paint)

        // Draw first letter of email address
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 40f
        textPaint.textAlign = Paint.Align.CENTER
        val initial = email.substring(0, 1).toUpperCase()
        canvas.drawText(initial, 50f, 65f, textPaint)

        // Return Drawable from created bitmap
        return BitmapDrawable(resources, bitmap)
    }

    private fun getColorFromEmail(email: String): Int {
        // Get color from email hashcode
        val hashCode = email.hashCode()
        return Color.HSVToColor(floatArrayOf(
            (hashCode and 0xFF) % 360.toFloat(),  // Hue
            0.6f,                                  // Saturation
            0.9f                                   // Value
        ))
    }
}

data class MasjidLocation(val name: String, val latitude: Double, val longitude: Double)