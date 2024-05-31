package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
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
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    if (user != null) {
                        checkUserRole(user.uid)
                    }
                } else {
                    Toast.makeText(this, "Login gagal, silakan coba lagi", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun checkUserRole(userId: String) {
        firestore.collection("user")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userData = document.data
                    if (userData != null) {
                        val role = userData["role"] as? String
                        if (role != null) {
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
                            return@addOnSuccessListener // Keluar dari fungsi setelah mengarahkan pengguna
                        }
                    }
                }
                // Jika tidak ada role atau ada kesalahan lain, arahkan ke halaman default (biasanya HomeActivity)
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
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
