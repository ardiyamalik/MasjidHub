package org.d3if0140.masjidhub

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.d3if0140.masjidhub.databinding.ActivityAdminKeuanganBinding

class AdminKeuangan : AppCompatActivity() {
    private lateinit var binding: ActivityAdminKeuanganBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminKeuanganBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data total yang dikirim dari aktivitas lain
        val totalInfaq = intent.getDoubleExtra("TOTAL_INFAQ", 0.0)
        val totalKas = intent.getDoubleExtra("TOTAL_KAS", 0.0)
        val totalPengajuan = intent.getDoubleExtra("TOTAL_PENGAJUAN", 0.0)

        val sharedPreferences = getSharedPreferences("AdminKeuanganPrefs", MODE_PRIVATE)
        val currentTotalInfaq = sharedPreferences.getFloat("totalInfaq", 0.0f).toDouble()
        val currentTotalKas = sharedPreferences.getFloat("totalKas", 0.0f).toDouble()
        val currentTotalPengajuan = sharedPreferences.getFloat("totalPengajuan", 0.0f).toDouble()

        // Hitung total yang baru
        val combinedTotal = (currentTotalInfaq + currentTotalKas) - totalPengajuan

        // Tampilkan total yang sudah diperbarui
        binding.keuangan.text = "Total Combined: $combinedTotal"

        // Simpan total yang sudah diperbarui
        val editor = sharedPreferences.edit()
        editor.putFloat("totalInfaq", currentTotalInfaq.toFloat())
        editor.putFloat("totalKas", currentTotalKas.toFloat())
        editor.putFloat("totalPengajuan", (currentTotalPengajuan + totalPengajuan).toFloat()) // Menyimpan total pengajuan baru
        editor.apply()

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.infaqButton.setOnClickListener {
            startActivity(Intent(this, LaporanInfaqActivity::class.java))
        }

        binding.kasButton.setOnClickListener {
            startActivity(Intent(this, LaporanKasActivity::class.java))
        }

        binding.pengajuanButton.setOnClickListener {
            startActivity(Intent(this, LaporanPengajuanActivity::class.java))
        }
    }
}
