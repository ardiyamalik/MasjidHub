package org.d3if0140.masjidhub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityGantiPasswordDkmBinding

class GantiPasswordDkmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGantiPasswordDkmBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGantiPasswordDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Ambil nama user dari Firestore dan tampilkan di TextView
            firestore.collection("user").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("nama") // Misalnya field nama disimpan dengan key 'nama'
                        binding.nama.text = userName
                    } else {
                        Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error mengambil data: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            // Kembali ke halaman sebelumnya jika tombol back ditekan
            binding.backButton.setOnClickListener{
                finish()
            }

            // Kirim email reset password jika tombol Lupa Password ditekan
            binding.LupaPassword.setOnClickListener {
                sendPasswordResetEmail()
            }

            // Menangani perubahan password saat tombol Simpan ditekan
            binding.buttonSimpan.setOnClickListener {
                val currentPassword = binding.passwordEditText.text.toString().trim()
                val newPassword = binding.passwordEditText2.text.toString().trim()
                val confirmPassword = binding.passwordEditText3.text.toString().trim()

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "Password baru tidak cocok", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                    Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Ambil data password dari Firestore
                firestore.collection("user").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val storedPassword = document.getString("password")

                            if (storedPassword == currentPassword) {
                                // Jika password saat ini benar, update password di Firebase Authentication
                                currentUser?.updatePassword(newPassword)
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Update juga password di Firestore untuk menyimpan perubahan
                                            firestore.collection("user").document(userId)
                                                .update("password", newPassword)
                                                .addOnSuccessListener {
                                                    Toast.makeText(this, "Password berhasil diganti", Toast.LENGTH_SHORT).show()
                                                    finish() // Kembali ke halaman sebelumnya setelah sukses
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(this, "Gagal mengganti password di Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            Toast.makeText(this, "Gagal mengganti password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(this, "Password saat ini salah", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error mengambil data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

        }
    }

    // Fungsi untuk mengirim email reset password ke pengguna yang sedang login
    private fun sendPasswordResetEmail() {
        val user = auth.currentUser

        if (user != null) {
            val email = user.email
            if (email != null) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Email reset password dikirim ke $email.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Gagal mengirim email reset password.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Email pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Tidak ada pengguna yang login.", Toast.LENGTH_SHORT).show()
        }
    }
}