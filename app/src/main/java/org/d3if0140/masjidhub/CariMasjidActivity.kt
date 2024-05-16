package org.d3if0140.masjidhub

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityCariMasjidBinding
import org.d3if0140.masjidhub.databinding.ActivityHomeBinding

class CariMasjidActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCariMasjidBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCariMasjidBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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

// Atur listener untuk bottom navigation view
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search_masjid -> {
                    // Tidak perlu lakukan apa pun jika pengguna sudah berada di halaman utama
                    true
                }
                R.id.menu_home -> {
                    // Arahkan ke CariMasjidActivity
                    val intent = Intent(this, HomeActivity::class.java)
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
