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
import com.bumptech.glide.Glide
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityDkmDashboardBinding


class DkmDashboard : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityDkmDashboardBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private val DEFAULT_ZOOM = 12f
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDkmDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase App Check
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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

        // Dapatkan ID pengguna yang saat ini masuk
        val currentUserId = mAuth.currentUser?.uid

        // Ambil data pengguna dari Firestore berdasarkan ID
        if (currentUserId != null) {
            firestore.collection("user")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userData = document.data
                        if (userData != null) {
                            val imageUrl = userData["imageUrl"] as? String

                            // Tampilkan foto profil jika URL tidak null
                            imageUrl?.let { loadProfileImage(it) } ?: run { displayDefaultProfileImage() }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal mengambil data pengguna: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Bottom navigation listener
        binding.bottomNavigationDkm.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home_dkm -> true
                R.id.uploadEvent -> {
                    val intent = Intent(this, UnggahActivity::class.java)
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
                R.id.menu_profile_dkm -> {
                    val intent = Intent(this, ProfilDkmActivity::class.java)
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

        // Event button
        binding.buttonEvent.setOnClickListener {
            val intent = Intent(this, EventActivity::class.java)
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

    private fun loadProfileImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(createAvatar(mAuth.currentUser?.email ?: ""))
            .into(binding.profileImageView)
    }

    private fun displayDefaultProfileImage() {
        val user = mAuth.currentUser
        if (user != null && user.email != null) {
            val email = user.email
            email?.let {
                val avatarDrawable = createAvatar(it)
                binding.profileImageView.setImageDrawable(avatarDrawable)
            }
        }
    }

    private fun createAvatar(email: String): Drawable {
        val color = getColorFromEmail(email)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { this.color = color }
        canvas.drawCircle(50f, 50f, 50f, paint)
        val textPaint = Paint().apply {
            this.color = Color.WHITE
            this.textSize = 40f
            this.textAlign = Paint.Align.CENTER
        }
        val initial = email.substring(0, 1).uppercase()
        canvas.drawText(initial, 50f, 65f, textPaint)
        return BitmapDrawable(resources, bitmap)
    }

    private fun getColorFromEmail(email: String): Int {
        val hashCode = email.hashCode()
        return Color.HSVToColor(floatArrayOf(
            (hashCode and 0xFF).toFloat() % 360,
            0.6f,
            0.9f
        ))
    }
}

