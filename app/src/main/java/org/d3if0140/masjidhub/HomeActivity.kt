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
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import org.d3if0140.masjidhub.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi SupportMapFragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Gambar untuk carousel
        val imageList = listOf(
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.banner3
        )

        val adapter = CarouselAdapter(imageList)
        binding.viewPager.adapter = adapter

        // Atur listener untuk bottom navigation view
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    // Tidak perlu lakukan apa pun jika pengguna sudah berada di halaman utama
                    true
                }
                R.id.search_masjid -> {
                    // Arahkan ke CariMasjidActivity
                    val intent = Intent(this, CariMasjidActivity::class.java)
                    startActivity(intent)
                    finish() // Akhiri aktivitas saat ini
                    true
                }
                R.id.menu_finance -> {
                    // Arahkan ke KeuanganActivity
                    val intent = Intent(this, KeuanganActivity::class.java)
                    startActivity(intent)
                    finish() // Akhiri aktivitas saat ini
                    true
                }
                R.id.menu_profile -> {
                    // Arahkan ke ProfilActivity
                    val intent = Intent(this, ProfilActivity::class.java)
                    startActivity(intent)
                    finish() // Akhiri aktivitas saat ini
                    true
                }
                // Tambahkan case untuk item lain jika diperlukan
                else -> false
            }
        }

        // Menambahkan onClickListener ke ImageView foto profil
        binding.profileImageView.setOnClickListener {
            // Membuat Intent untuk memulai ProfileActivity
            val intent = Intent(this, ProfilActivity::class.java)
            startActivity(intent)
        }

        // Menampilkan foto profil default berdasarkan email pengguna
        displayDefaultProfileImage()
    }

    override fun onMapReady(map: GoogleMap?) {
        map?.let {
            googleMap = it

            // Check permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                return
            }

            // Enable user's current location
            googleMap.isMyLocationEnabled = true

            // Move camera to user's current location
            val myLocation = LatLng(-7.025986985493635, 107.54172813940146)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))

            // Add marker for user's current location
            val markerOptions = MarkerOptions()
                .position(myLocation)
                .title("My Location")
            googleMap.addMarker(markerOptions)
        }
    }

    private fun displayDefaultProfileImage() {
        // Mendapatkan pengguna yang saat ini masuk (login).
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            // Mendapatkan alamat email pengguna.
            val email = user.email
            email?.let {
                // Ubah alamat email menjadi avatar dan tampilkan di ImageView
                val avatarDrawable = createAvatar(it)
                binding.profileImageView.setImageDrawable(avatarDrawable)
            }
        }
    }

    private fun createAvatar(email: String): Drawable {
        // Mendapatkan warna dari hashcode alamat email.
        val color = getColorFromEmail(email)

        // Membuat bitmap kosong untuk avatar.
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Menggambar latar belakang berwarna.
        val paint = Paint()
        paint.color = color
        canvas.drawCircle(50f, 50f, 50f, paint)

        // Menggambar huruf pertama dari alamat email.
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 40f
        textPaint.textAlign = Paint.Align.CENTER
        val initial = email.substring(0, 1).toUpperCase()
        canvas.drawText(initial, 50f, 65f, textPaint)

        // Mengembalikan Drawable dari bitmap yang dibuat.
        return BitmapDrawable(resources, bitmap)
    }

    private fun getColorFromEmail(email: String): Int {
        // Menggunakan hashcode dari alamat email untuk mendapatkan warna.
        val hashCode = email.hashCode()
        return Color.HSVToColor(floatArrayOf(
            (hashCode and 0xFF) % 360.toFloat(),  // Hue
            0.6f,                                  // Saturation
            0.9f                                   // Value
        ))
    }
}
