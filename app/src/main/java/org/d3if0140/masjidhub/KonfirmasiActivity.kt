package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.d3if0140.masjidhub.databinding.ActivityKonfirmasiBinding

class KonfirmasiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKonfirmasiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKonfirmasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menambahkan onClickListener pada tombol login
        binding.buttonLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Menutup aktivitas VerifActivity agar tidak dapat diakses lagi setelah kembali ke LoginActivity
        }
    }
}