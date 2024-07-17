package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.d3if0140.masjidhub.databinding.ActivityAdminKeuanganBinding

class AdminKeuangan : AppCompatActivity() {
    private lateinit var binding: ActivityAdminKeuanganBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminKeuanganBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menambahkan onClickListener pada button backButton untuk kembali ke AdminDashboard
        binding.backButton.setOnClickListener {
            val intent = Intent(this, AdminDashboard::class.java)
            startActivity(intent)
        }

        binding.infaqButton.setOnClickListener{
            val intent = Intent(this, LaporanInfaqActivity::class.java)
            startActivity(intent)
        }

        binding.kasButton.setOnClickListener{
            val intent = Intent(this, LaporanKasActivity::class.java)
            startActivity(intent)
        }

        binding.pengajuanButton.setOnClickListener{
            val intent = Intent(this, LaporanPengajuanActivity::class.java)
            startActivity(intent)
        }
    }
}