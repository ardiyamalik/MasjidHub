package org.d3if0140.masjidhub

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityProfilDkmBinding

class ProfilDkmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfilDkmBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Setup TabLayout
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showFragment(PostingFragment())
                    1 -> showFragment(InfaqFragment())
                    2 -> showFragment(JamaahTerdaftarFragment().apply {
                    arguments = Bundle().apply {
                        putString("nama", binding.namaUserDkm.text.toString())
                    }
                })
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Atur listener untuk tombol jamaahYangTerdaftar menjadi PopupMenu
        binding.menu.setOnClickListener { view ->
            val popupMenuDkm = androidx.appcompat.widget.PopupMenu(this, view)
            popupMenuDkm.menuInflater.inflate(R.menu.popup_menu_dkm, popupMenuDkm.menu)

            popupMenuDkm.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.logout -> {
                        // Handle logout
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("Apakah Anda yakin ingin logout?")
                            .setCancelable(false)
                            .setPositiveButton("Ya") { dialog, id ->
                                mAuth.signOut()
                                val intent = Intent(this, WelcomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .setNegativeButton("Tidak") { dialog, id ->
                                dialog.dismiss()
                            }
                        val alert = builder.create()
                        alert.show()
                        true
                    }
                    R.id.informasiKas -> {
                        // Arahkan ke halaman Informasi Kas
                        val intent = Intent(this, InformasiKasDkmActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popupMenuDkm.show()
        }

        // Atur listener untuk tombol ubah profil
        binding.ubahProfileDkm.setOnClickListener {
            val intent = Intent(this, UbahProfilDkm::class.java)
            startActivity(intent)
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
                            val nama = userData["nama"] as? String
                            val alamat = userData["alamat"] as? String
                            val imageUrl = userData["imageUrl"] as? String

                            // Set OnClickListener to open image in full screen
                            binding.profileImageDkm.setOnClickListener {
                                val intent = Intent(this, FullScreenImageActivity::class.java)
                                intent.putExtra("IMAGE_URL", imageUrl)
                                startActivity(intent)
                            }

                            // Tampilkan nama pengguna jika tidak null
                            nama?.let { binding.namaUserDkm.text = it }

                            // Tampilkan alamat masjid
                            alamat?.let { binding.alamatMasjid.text = it }

                            // Tampilkan foto profil jika URL tidak null
                            imageUrl?.let { loadProfileImage(it) }
                                ?: run { displayDefaultProfileImage() }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Gagal mengambil data pengguna: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // Bottom navigation listener
        binding.bottomNavigationDkm.selectedItemId = R.id.menu_profile_dkm
        binding.bottomNavigationDkm.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile_dkm -> true
                R.id.menu_home_dkm -> {
                    val intent = Intent(this, DkmDashboard::class.java)
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

                R.id.unggahEvent -> {
                    val intent = Intent(this, UnggahActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                else -> false
            }
        }
    }

    private fun loadProfileImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(createAvatar(mAuth.currentUser?.email ?: ""))
            .into(binding.profileImageDkm)
    }

    private fun displayDefaultProfileImage() {
        val user = mAuth.currentUser
        if (user != null && user.email != null) {
            val email = user.email
            email?.let {
                val avatarDrawable = createAvatar(it)
                binding.profileImageDkm.setImageDrawable(avatarDrawable)
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

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
