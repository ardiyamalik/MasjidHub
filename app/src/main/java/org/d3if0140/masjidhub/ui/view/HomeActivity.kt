package org.d3if0140.masjidhub.ui.view

import org.d3if0140.masjidhub.ui.adapter.CarouselAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityHomeBinding
import android.Manifest
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.ui.adapter.PengurusDkmAdapter
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.model.PengurusDkm

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: PengurusDkmAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null
    private lateinit var carouselAdapter: CarouselAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inisialisasi Firebase App Check
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Setup carousel di ViewPager
        setupCarousel()

        // Cek izin lokasi
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnCompleteListener(OnCompleteListener<Location> { task ->
                if (task.isSuccessful && task.result != null) {
                    userLocation = task.result
                    setupPengurusDkmRecyclerView()
                }
            })
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

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
        binding.bottomNavigation.selectedItemId = R.id.menu_home
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
                    val intent = Intent(this, LaporanKeuanganActivity::class.java)
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

        // Event button
        binding.buttonEvent.setOnClickListener {
            val intent = Intent(this, EventActivity::class.java)
            startActivity(intent)
        }

        // infaq button
        binding.buttonInfaq.setOnClickListener{
            val intent = Intent(this, InfaqActivity::class.java)
            startActivity(intent)
        }

        // Notification Button
        binding.buttonNotif.setOnClickListener{
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        // Display default profile image based on user's email
        displayDefaultProfileImage()
    }

    private fun setupCarousel() {
        // Ambil gambar carousel dari Firestore, urutkan berdasarkan timestamp menurun dan batasi hasil menjadi 5
        firestore.collection("carousel")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val imageList = mutableListOf<String>()
                for (document in documents) {
                    val imageUrl = document.getString("imageUrl")
                    imageUrl?.let { imageList.add(it) }
                }

                // Setup ViewPager dengan org.d3if0140.masjidhub.ui.adapter.CarouselAdapter
                carouselAdapter = CarouselAdapter(imageList)
                binding.viewPager.adapter = carouselAdapter
            }
            .addOnFailureListener {
                // Handle failure
                Toast.makeText(this, "Gagal mengambil gambar carousel: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupPengurusDkmRecyclerView() {
        binding.recyclerViewPengurus.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val adapter = PengurusDkmAdapter { pengurusDkm ->
            val intent = Intent(this, ProfileSearchActivity::class.java)
            intent.putExtra("USER_ID", pengurusDkm.userId)
            intent.putExtra("USER_NAME", pengurusDkm.nama)
            intent.putExtra("USER_ALAMAT", pengurusDkm.alamat)
            intent.putExtra("USER_IMAGE_URL", pengurusDkm.imageUrl)
            startActivity(intent)
        }

        binding.recyclerViewPengurus.adapter = adapter

        firestore.collection("user")
            .whereEqualTo("role", "pengurus_dkm")
            .get()
            .addOnSuccessListener { result ->
                val pengurusDkmList = mutableListOf<PengurusDkm>()
                for (document in result) {
                    val nama = document.getString("nama") ?: ""
                    val alamat = document.getString("alamat") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val userId = document.id

                    // Buat objek Location untuk pengurus
                    val pengurusLocation = Location("").apply {
                        this.latitude = latitude
                        this.longitude = longitude
                    }

                    // Hitung jarak dari lokasi pengguna ke lokasi pengurus
                    val distanceInMeters = userLocation?.distanceTo(pengurusLocation) ?: 0f

                    // Jika jarak kurang dari 10km, tambahkan ke daftar
                    if (distanceInMeters <= 10000) {
                        pengurusDkmList.add(PengurusDkm(nama, alamat, imageUrl, userId))
                    }
                }
                adapter.setData(pengurusDkmList)

                // Jika tidak ada data yang sesuai
                if (pengurusDkmList.isEmpty()) {
                    binding.recyclerViewPengurus.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mengambil data pengurus_dkm: ${exception.message}", Toast.LENGTH_SHORT).show()
            }


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
