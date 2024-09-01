package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.d3if0140.masjidhub.databinding.ActivityKeuanganDkmBinding

class KeuanganDkmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeuanganDkmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeuanganDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.buttonLaporanInfaq.setOnClickListener {
            val intent = Intent(this, LaporanInfaqDkm::class.java)
            startActivity(intent)
        }

        binding.buttonLaporanKas.setOnClickListener {
            val intent = Intent(this, LaporanKasDkm::class.java)
            startActivity(intent)
        }

        binding.buttonLaporanPengajuan.setOnClickListener {
            val intent = Intent(this, LaporanPengajuanDkm::class.java)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
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
}
