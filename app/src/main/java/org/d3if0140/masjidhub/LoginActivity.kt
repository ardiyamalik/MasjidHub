package org.d3if0140.masjidhub

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase App Check
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Mengatur tombol kembali ke halaman WelcomeActivity
        binding.backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        // Mengatur TextView "Lupa Kata sandi? Klik disini"
        val text = "Lupa Kata sandi? Klik disini"
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf("Klik disini")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // Mengambil email dari EditText untuk dikirimkan email reset password
                val email = binding.namaEditText.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Masukkan email terlebih dahulu", Toast.LENGTH_SHORT).show()
                    return
                }
                sendPasswordResetEmail(email)
            }
        }
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            startIndex + 10, // "Klik disini" memiliki panjang 10 karakter
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textLupa.text = spannableString
        binding.textLupa.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        // Toggle visibility password
        binding.passwordToggle.setOnClickListener {
            val inputType = binding.passwordEditText.inputType
            if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                binding.passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.passwordToggle.setImageResource(R.drawable.baseline_visibility_off)
            } else {
                binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.passwordToggle.setImageResource(R.drawable.baseline_visibility)
            }
            binding.passwordEditText.setSelection(binding.passwordEditText.text?.length ?: 0)
        }

        // Implementasi login
        binding.loginButton.setOnClickListener {
            val email = binding.namaEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            Log.d("LoginActivity", "Attempting to log in with email: $email")
            loginUser(email, password)
        }

        // Implementasi Lupa Password
        binding.textLupa.setOnClickListener {
            val email = binding.namaEditText.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Silakan masukkan email Anda", Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(email)
            }
        }
    }

    // Fungsi untuk login
    private fun loginUser(email: String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Harap Tunggu...")
        progressDialog.show()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss() // Tutup dialog setelah login selesai (baik berhasil atau gagal)
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    if (user != null) {
                        Log.d("LoginActivity", "Login successful for user: ${user.uid}")
                        checkUserRole(user.uid)
                    }
                } else {
                    Log.e("LoginActivity", "Login failed: ${task.exception?.message}")
                    Toast.makeText(this, "Email atau Password salah", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Fungsi untuk mengirim email reset password
    private fun sendPasswordResetEmail(email: String) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email reset password berhasil dikirim", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal mengirim email reset password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Fungsi untuk memeriksa role pengguna setelah login
    private fun checkUserRole(userId: String) {
        Log.d("LoginActivity", "Checking role for user: $userId")
        firestore.collection("user")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userData = document.data
                    if (userData != null) {
                        val role = userData["role"] as? String
                        val verified = userData["verified"] as? Boolean

                        Log.d("LoginActivity", "User role: $role, Verified: $verified")

                        if (role == "pengurus_dkm" && verified == false) {
                            Toast.makeText(this, "Akun Anda belum diverifikasi oleh admin", Toast.LENGTH_SHORT).show()
                        } else {
                            when (role) {
                                "admin" -> {
                                    val intent = Intent(this, AdminDashboard::class.java)
                                    startActivity(intent)
                                }
                                "pengurus_dkm" -> {
                                    val intent = Intent(this, DkmDashboard::class.java)
                                    startActivity(intent)
                                }
                                else -> {
                                    val intent = Intent(this, HomeActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                            finish()
                        }
                    }
                } else {
                    Log.d("LoginActivity", "No user data found, redirecting to WelcomeActivity")
                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LoginActivity", "Failed to check user role: ${exception.message}")
                Toast.makeText(this, "Gagal memeriksa peran pengguna: ${exception.message}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            }
    }
}
