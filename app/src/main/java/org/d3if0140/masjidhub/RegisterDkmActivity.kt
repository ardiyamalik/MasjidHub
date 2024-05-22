package org.d3if0140.masjidhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
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

        // Tambahkan OnClickListener pada hubungiAdminButton
        binding.hubungiAdminButton.setOnClickListener {
            // Nomor telepon tujuan (contoh: +6281234567890)
            val phoneNumber = "+6281234567890"
            // Buat URI untuk membuka WhatsApp
            val uri = Uri.parse("https://wa.me/$phoneNumber")
            // Buat Intent dengan ACTION_VIEW dan URI yang sudah dibuat
            val intent = Intent(Intent.ACTION_VIEW, uri)
            // Periksa apakah ada aplikasi yang dapat menangani Intent ini
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Berikan pesan atau lakukan tindakan lain jika tidak ada aplikasi yang dapat menangani Intent
                // Misalnya, Anda bisa menggunakan Toast untuk memberi tahu pengguna
                Toast.makeText(this, "Aplikasi WhatsApp tidak ditemukan.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
