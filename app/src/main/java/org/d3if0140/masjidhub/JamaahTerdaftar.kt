package org.d3if0140.masjidhub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.d3if0140.masjidhub.databinding.ActivityJamaahTerdaftarBinding

class JamaahTerdaftar : AppCompatActivity() {

    private lateinit var binding: ActivityJamaahTerdaftarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi binding
        binding = ActivityJamaahTerdaftarBinding.inflate(layoutInflater)

        // Set view menggunakan binding.root
        setContentView(binding.root)


    }
}
