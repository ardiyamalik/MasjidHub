package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.d3if0140.masjidhub.databinding.ActivityCariMasjidBinding
import org.d3if0140.masjidhub.databinding.ActivityHomeBinding

class CariMasjidActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCariMasjidBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCariMasjidBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }
}