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

        binding.buttonEvent.setOnClickListener{
            startActivity(Intent(this, AdminEvent::class.java))
        }

        binding.buttonKeuangan.setOnClickListener{
            startActivity(Intent(this, AdminKeuangan::class.java))
        }

        binding.buttonAjuan.setOnClickListener{
            startActivity(Intent(this, AdminPermohonan::class.java))
        }

        binding.buttonInfaqAdmin.setOnClickListener{
            startActivity(Intent(this, AdminInfaqActivity::class.java))
        }
//        binding.buttonKas.setOnClickListener{
//            startActivity(Intent(this, KasMingguanActivity::class.java))
//        }

        binding.buttonVerifAdmin.setOnClickListener{
            startActivity(Intent(this, AdminVerifActivity::class.java))
        }
    }
}