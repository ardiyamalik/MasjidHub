package org.d3if0140.masjidhub.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
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
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Apakah Anda yakin ingin logout?")
                .setCancelable(false)
                .setPositiveButton("Ya") { dialog, id ->
                    mAuth.signOut()
                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Tidak") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        //Mengarahkan ke Halaman
        binding.buttonMasjid.setOnClickListener{
            startActivity(Intent(this, AdminListMasjid::class.java))
        }

        binding.buttonEvent.setOnClickListener{
            startActivity(Intent(this, AdminEvent::class.java))
        }

        binding.buttonKeuangan.setOnClickListener{
            startActivity(Intent(this, AdminLaporanKeuangan::class.java))
        }

        binding.buttonAjuan.setOnClickListener{
            startActivity(Intent(this, AdminPermohonan::class.java))
        }

        binding.buttonInfaqAdmin.setOnClickListener{
            startActivity(Intent(this, AdminInfaqActivity::class.java))
        }

        binding.buttonKasMingguan.setOnClickListener{
            startActivity(Intent(this, KasMingguanActivity::class.java))
        }

        binding.buttonVerifAdmin.setOnClickListener{
            startActivity(Intent(this, AdminVerifActivity::class.java))
        }

        binding.buttonUploadCarousel.setOnClickListener{
            startActivity(Intent(this, AdminUploadCarousel::class.java))
        }
    }
}