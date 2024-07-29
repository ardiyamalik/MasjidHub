package org.d3if0140.masjidhub

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.d3if0140.masjidhub.databinding.ActivityAdminKeuanganBinding

class AdminKeuangan : AppCompatActivity() {
    private lateinit var binding: ActivityAdminKeuanganBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminKeuanganBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("AdminKeuanganPrefs", MODE_PRIVATE)

        // Ambil data total yang dikirim dari aktivitas lain
        val totalInfaq = intent.getDoubleExtra("TOTAL_INFAQ", 0.0)
        val totalKas = intent.getDoubleExtra("TOTAL_KAS", 0.0)
        val totalPengajuan = intent.getDoubleExtra("TOTAL_PENGAJUAN", 0.0)

        // Ambil total yang tersimpan sebelumnya
        val currentTotalInfaq = sharedPreferences.getFloat("totalInfaq", 0.0f).toDouble()
        val currentTotalKas = sharedPreferences.getFloat("totalKas", 0.0f).toDouble()
        val currentTotalPengajuan = sharedPreferences.getFloat("totalPengajuan", 0.0f).toDouble()

        // Hitung total yang baru
        val combinedTotal = (currentTotalInfaq + totalInfaq + currentTotalKas + totalKas) - (currentTotalPengajuan + totalPengajuan)

        // Tampilkan total yang sudah diperbarui
        binding.keuangan.text = "Total Combined: $combinedTotal"

        // Simpan total yang sudah diperbarui
        val editor = sharedPreferences.edit()
        editor.putFloat("totalInfaq", (currentTotalInfaq + totalInfaq).toFloat())
        editor.putFloat("totalKas", (currentTotalKas + totalKas).toFloat())
        editor.putFloat("totalPengajuan", (currentTotalPengajuan + totalPengajuan).toFloat())
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

        binding.resetButton.setOnClickListener {
            resetTotals()
        }

        binding.submitButton.setOnClickListener {
            updateAndSendData()
        }
    }

    private fun resetTotals() {
        val sharedPreferences = getSharedPreferences("AdminKeuanganPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("totalInfaq", 0.0f)
        editor.putFloat("totalKas", 0.0f)
        editor.putFloat("totalPengajuan", 0.0f)
        editor.apply()

        // Update UI
        binding.keuangan.text = "Total Combined: 0.0"
    }

    private fun updateAndSendData() {
        val sharedPreferences = getSharedPreferences("AdminKeuanganPrefs", MODE_PRIVATE)
        val totalInfaq = sharedPreferences.getFloat("totalInfaq", 0.0f)
        val totalKas = sharedPreferences.getFloat("totalKas", 0.0f)
        val totalPengajuan = sharedPreferences.getFloat("totalPengajuan", 0.0f)

        // Kirim data melalui broadcast
        sendBroadcast(totalInfaq, totalKas, totalPengajuan)

        // Tampilkan pesan toast
        Toast.makeText(this, "Keuangan berhasil diperbarui", Toast.LENGTH_SHORT).show()
    }

    private fun sendBroadcast(totalInfaq: Float, totalKas: Float, totalPengajuan: Float) {
        val intent = Intent("org.d3if0140.masjidhub.UPDATE_KEUANGAN")
        intent.putExtra("TOTAL_INFAQ", totalInfaq)
        intent.putExtra("TOTAL_KAS", totalKas)
        intent.putExtra("TOTAL_PENGAJUAN", totalPengajuan)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
