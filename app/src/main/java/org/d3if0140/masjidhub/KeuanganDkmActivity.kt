package org.d3if0140.masjidhub

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.d3if0140.masjidhub.databinding.ActivityKeuanganDkmBinding

class KeuanganDkmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeuanganDkmBinding

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
        binding = ActivityKeuanganDkmBinding.inflate(layoutInflater)
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
            val intent = Intent(this, LaporanInfaqDkm::class.java)
            startActivity(intent)
        }

        binding.buttonLaporanKas.setOnClickListener{
            val intent = Intent(this, LaporanKasDkm::class.java)
            startActivity(intent)
        }

        binding.buttonLaporanPengajuan.setOnClickListener{
            val intent = Intent(this, LaporanPengajuanDkm::class.java)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener{
            startActivity(Intent(this, DkmDashboard::class.java))
        }

        // Bottom navigation listener
        binding.bottomNavigationDkm.selectedItemId = R.id.menu_finance
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
                    val intent = Intent(this, KeuanganDkmActivity::class.java)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keuanganReceiver)
    }
}
