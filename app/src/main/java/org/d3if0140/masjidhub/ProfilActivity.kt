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
import com.google.firebase.auth.FirebaseAuth
import org.d3if0140.masjidhub.databinding.ActivityProfilBinding

class ProfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfilBinding
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

// Atur listener untuk bottom navigation view
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
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
                R.id.menu_home -> {
                    // Arahkan ke ProfilActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish() // Akhiri aktivitas saat ini
                    true
                }
                // Tambahkan case untuk item lain jika diperlukan
                else -> false
            }
        }

        // Menampilkan foto profil default berdasarkan email pengguna
        displayDefaultProfileImage()

        // Menampilkan nama pengguna
        displayUserName()
    }

    private fun displayUserName() {
        // Mendapatkan pengguna yang saat ini masuk (login).
        val user = mAuth.currentUser

        // Menampilkan nama pengguna
        user?.displayName?.let { displayName ->
            binding.namaUser.text = displayName
        }
    }

    private fun displayDefaultProfileImage() {
        // Mendapatkan pengguna yang saat ini masuk (login).
        val user = mAuth.currentUser

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
