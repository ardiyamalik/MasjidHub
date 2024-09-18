package org.d3if0140.masjidhub.ui.view

import org.d3if0140.masjidhub.ui.adapter.CarouselAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.ActivityDkmDashboardBinding

class DkmDashboard : AppCompatActivity() {
    private lateinit var binding: ActivityDkmDashboardBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var carouselAdapter: CarouselAdapter

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

        // Setup carousel di ViewPager
        setupCarousel()

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
        binding.bottomNavigationDkm.selectedItemId = R.id.menu_home_dkm
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
                    val intent = Intent(this, LaporanKeuanganDkmActivity::class.java)
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
            val intent = Intent(this, ProfilDkmActivity::class.java)
            startActivity(intent)
        }

        // Event button
        binding.buttonEvent.setOnClickListener {
            val intent = Intent(this, EventActivity::class.java)
            startActivity(intent)
        }

        // Pengajuan Dana Button
        binding.buttonPengajuan.setOnClickListener {
            val intent = Intent(this, PengajuanDanaActivity::class.java)
            startActivity(intent)
        }

        //Button Notif
        binding.buttonNotif.setOnClickListener {
            val intent = Intent(this, NotificationDkmActivity::class.java)
            startActivity(intent)
        }

        //Button Kas
        binding.buttonKas.setOnClickListener {
            val intent = Intent(this, PengisianKasActivity::class.java)
            startActivity(intent)
        }

        // Gunakan masjidId di sini saat menavigasi ke aktivitas lain
        binding.buttonPengajuan.setOnClickListener {
            val intent = Intent(this, PengajuanDanaActivity::class.java)
            startActivity(intent)
        }

        binding.buttonInputNeraca.setOnClickListener{
            val intent = Intent(this, InputNeracaActivity::class.java)
            intent.putExtra("userId", currentUserId)
            startActivity(intent)

        }

        binding.buttonTampilkanNeraca.setOnClickListener{
            val intent = Intent(this, TampilkanNeracaActivity::class.java)
            intent.putExtra("userId", currentUserId) // Kirim userId
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
