package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.d3if0140.masjidhub.databinding.ActivityAdminEventBinding

class AdminEvent : AppCompatActivity() {
    private lateinit var binding: ActivityAdminEventBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        // Menambahkan onClickListener pada button backButton untuk kembali ke AdminDashboard
//        binding.backButton.setOnClickListener {
//            val intent = Intent(this, AdminDashboard::class.java)
//            startActivity(intent)
//        }
    }
}