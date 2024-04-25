package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.d3if0140.masjidhub.databinding.ActivityRegistDkmBinding



class RegisterDkmActivity : AppCompatActivity() {
    // Deklarasi variabel binding untuk menggunakan ViewBinding
    private lateinit var binding: ActivityRegistDkmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menggunakan inflate pada binding untuk menghubungkan layout XML dengan Activity
        binding = ActivityRegistDkmBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}