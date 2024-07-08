package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.method.LinkMovementMethod
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

        binding.backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        val text = "Belum punya akun? Daftar"
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf("Daftar")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val intent = Intent(this@LoginActivity, RegistActivity::class.java)
                startActivity(intent)
            }
        }
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            startIndex + 4,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textDaftar.text = spannableString
        binding.textDaftar.movementMethod = LinkMovementMethod.getInstance()

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

        binding.loginButton.setOnClickListener {
            val email = binding.namaEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            Log.d("LoginActivity", "Attempting to log in with email: $email")
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    if (user != null) {
                        Log.d("LoginActivity", "Login successful for user: ${user.uid}")
                        checkUserRole(user.uid)
                    }
                } else {
                    Log.e("LoginActivity", "Login failed: ${task.exception?.message}")
                    Toast.makeText(this, "Login gagal, silakan coba lagi", Toast.LENGTH_SHORT).show()
                }
            }
    }

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
                            // Akun pengurus_dkm yang belum diverifikasi
                            Toast.makeText(this, "Akun Anda belum diverifikasi oleh admin", Toast.LENGTH_SHORT).show()
                        } else {
                            // Akun telah diverifikasi atau bukan pengurus_dkm, arahkan sesuai role
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
                    // Jika tidak ada data pengguna atau dokumen tidak ada, arahkan ke WelcomeActivity
                    Log.d("LoginActivity", "No user data found, redirecting to WelcomeActivity")
                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LoginActivity", "Failed to check user role: ${exception.message}")
                Toast.makeText(
                    this,
                    "Gagal memeriksa peran pengguna: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                // Jika gagal, arahkan ke halaman default (biasanya HomeActivity)
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
    }

}
