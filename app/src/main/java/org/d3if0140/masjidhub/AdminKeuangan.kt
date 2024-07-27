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

        val sharedPreferences = getSharedPreferences("AdminKeuanganPrefs", MODE_PRIVATE)
        val totalInfaq = sharedPreferences.getFloat("totalInfaq", 0.0f).toDouble()
        val totalKas = sharedPreferences.getFloat("totalKas", 0.0f).toDouble()

        // Calculate combined total
        val combinedTotal = totalInfaq + totalKas

        // Display the combined total
        binding.keuangan.text = "Total Combined: $combinedTotal"

        binding.backButton.setOnClickListener {
            finish() // Close this activity
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
