package org.d3if0140.masjidhub.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    // Deklarasi variabel binding untuk menggunakan ViewBinding
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menggunakan inflate pada binding untuk menghubungkan layout XML dengan Activity
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Memeriksa apakah pengguna sudah login sebelumnya
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Jika sudah login, langsung navigasikan ke halaman home
            navigateToHome()
            // Selesai agar tidak lanjut ke bagian bawah onCreate
            return
        }

        // Menambahkan onClickListener pada button login untuk memulai LoginActivity
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Menambahkan onClickListener pada button register untuk memulai RegistActivity
        binding.registerButton.setOnClickListener {
            val intent = Intent(this, RegistActivity::class.java)
            startActivity(intent)
        }
        binding.regDkmButton.setOnClickListener {
            val intent = Intent(this, RegisterDkmActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToHome() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("user").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val role = document.getString("role")
                        when (role) {
                            "jamaah" -> startActivity(Intent(this, HomeActivity::class.java))
                            "pengurus_dkm" -> startActivity(Intent(this, DkmDashboard::class.java))
                            "admin" -> startActivity(Intent(this, AdminDashboard::class.java))
                            else -> startActivity(Intent(this, WelcomeActivity::class.java))
                        }
                        finish() // agar pengguna tidak dapat kembali ke halaman welcome dengan menekan tombol back
                    } else {
                        // Handle the case where the document does not exist
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
    }
}
