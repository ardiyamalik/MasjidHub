package org.d3if0140.masjidhub

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.d3if0140.masjidhub.databinding.ActivityKeuanganBinding

class KeuanganActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeuanganBinding

    private val keuanganReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val totalInfaq = it.getFloatExtra("TOTAL_INFAQ", 0.0f).toDouble()
                val totalKas = it.getFloatExtra("TOTAL_KAS", 0.0f).toDouble()
                val totalPengajuan = it.getFloatExtra("TOTAL_PENGAJUAN", 0.0f).toDouble()

                val combinedTotal = totalInfaq + totalKas - totalPengajuan

                binding.uangTerkumpul.text = "Total Combined: $combinedTotal"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeuanganBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            keuanganReceiver,
            IntentFilter("org.d3if0140.masjidhub.UPDATE_KEUANGAN")
        )

        // Ambil data dari SharedPreferences untuk pertama kali
        val sharedPreferences = getSharedPreferences("AdminKeuanganPrefs", MODE_PRIVATE)
        val totalInfaq = sharedPreferences.getFloat("totalInfaq", 0.0f).toDouble()
        val totalKas = sharedPreferences.getFloat("totalKas", 0.0f).toDouble()
        val totalPengajuan = sharedPreferences.getFloat("totalPengajuan", 0.0f).toDouble()

        val combinedTotal = totalInfaq + totalKas - totalPengajuan

        binding.uangTerkumpul.text = "Total Combined: $combinedTotal"

        binding.buttonLaporanInfaq.setOnClickListener{
            val intent = Intent(this, LaporanInfaqJamaah::class.java)
            startActivity(intent)
        }

        binding.buttonLaporanKas.setOnClickListener{
            val intent = Intent(this, LaporanKasJamaah::class.java)
            startActivity(intent)
        }

        binding.buttonLaporanPengajuan.setOnClickListener{
            val intent = Intent(this, LaporanPengajuanJamaah::class.java)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener{
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // Atur listener untuk bottom navigation view
        binding.bottomNavigation.selectedItemId = R.id.menu_finance
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_finance -> {
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
                R.id.menu_home -> {
                    // Arahkan ke KeuanganActivity
                    val intent = Intent(this, HomeActivity::class.java)
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

    override fun onDestroy() {
        super.onDestroy()
        // Unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keuanganReceiver)
    }
}


