package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityAdminDashboardBinding

class AdminDashboard : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Atur listener untuk tombol logout
        binding.buttonLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

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