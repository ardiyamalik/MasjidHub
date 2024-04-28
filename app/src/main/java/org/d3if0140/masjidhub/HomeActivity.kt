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

            // Tampilkan marker lokasi pengguna saat ini
            showCurrentLocation()
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
                    googleMap.addMarker(
                        MarkerOptions().position(currentLatLng).title("Lokasi Anda")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))

                    // Tampilkan masjid terdekat
                    showNearestMosque(currentLatLng)
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

    private fun showNearestMosque(currentLatLng: LatLng) {
        // Buat request untuk mencari masjid terdekat
        val request = FindCurrentPlaceRequest.newInstance(
            listOf(Place.Field.NAME, Place.Field.LAT_LNG)
        )
        val placeResponse = placesClient.findCurrentPlace(request)
        placeResponse.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val likelyPlaces = task.result?.placeLikelihoods
                likelyPlaces?.let { places ->
                    for (placeLikelihood in places) {
                        val place = placeLikelihood.place
                        val types = place.types ?: continue // Melanjutkan iterasi jika types null

                        // Tambahkan marker untuk tempat (place) yang merupakan masjid
                        if (types.contains(Place.Type.MOSQUE)) { // Periksa apakah tempat tersebut masjid
                            val mosqueLocation = LatLng(
                                place.latLng!!.latitude,
                                place.latLng!!.longitude
                            )

                            // Tambahkan marker untuk masjid dengan ikon dan judul
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(mosqueLocation)
                                    .title(place.name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            )
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Gagal mendapatkan tempat-tempat terdekat.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
