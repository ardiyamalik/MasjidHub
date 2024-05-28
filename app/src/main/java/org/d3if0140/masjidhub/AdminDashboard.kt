package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.d3if0140.masjidhub.databinding.ActivityAdminDashboardBinding

class AdminDashboard : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Mengarahkan ke Halaman
        binding.buttonMasjid.setOnClickListener{
            startActivity(Intent(this, AdminListMasjid::class.java))
        }

        binding.buttonAgenda.setOnClickListener{
            startActivity(Intent(this, AdminEvent::class.java))
        }

        binding.buttonKeuangan.setOnClickListener{
            startActivity(Intent(this, AdminKeuangan::class.java))
        }

        binding.buttonAjuan.setOnClickListener{
            startActivity(Intent(this, AdminIsiPermohonan::class.java))
        }
    }
}