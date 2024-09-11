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

        // Initialize Firebase App Check
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Back button to WelcomeActivity
        binding.backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        // Setup "Forgot Password?" clickable text
        val text = "Lupa Kata sandi? Klik disini"
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf("Klik disini")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // Send password reset email
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
            startIndex + 10, // "Klik disini" has a length of 10 characters
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textLupa.text = spannableString
        binding.textLupa.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        // Toggle password visibility
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

        // Implement login
        binding.loginButton.setOnClickListener {
            val email = binding.namaEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            Log.d("LoginActivity", "Attempting to log in with email: $email")
            loginUser(email, password)
        }

        // Implement password reset
        binding.textLupa.setOnClickListener {
            val email = binding.namaEditText.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Silakan masukkan email Anda", Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(email)
            }
        }
    }

    // Function for login
    private fun loginUser(email: String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Harap Tunggu...")
        progressDialog.show()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss() // Close dialog after login completes
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    if (user != null) {
                        Log.d("LoginActivity", "Login successful for user: ${user.uid}")
                        checkUserRole(user.uid, password)
                    }
                } else {
                    Log.e("LoginActivity", "Login failed: ${task.exception?.message}")
                    Toast.makeText(this, "Email atau Password salah", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to send password reset email
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

    // Function to check user role and verification status
    private fun checkUserRole(userId: String, inputPassword: String) {
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
                        val storedPassword = userData["password"] as? String

                        Log.d("LoginActivity", "User role: $role, Verified: $verified")

                        // Check if the input password is different from the stored one in Firestore
                        if (storedPassword != inputPassword) {
                            firestore.collection("user").document(userId)
                                .update("password", inputPassword)
                                .addOnSuccessListener {
                                    Log.d("LoginActivity", "Password updated in Firestore")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("LoginActivity", "Failed to update password in Firestore: ${e.message}")
                                }
                        }

                        when {
                            role == "pengurus_dkm" && verified == false -> {
                                Toast.makeText(this, "Akun Anda belum diverifikasi atau ditolak oleh admin", Toast.LENGTH_SHORT).show()
                                // Log out the user
                                mAuth.signOut()
                            }
                            role == "jamaah" && verified == false -> {
                                // Check if email is verified
                                val user = mAuth.currentUser
                                if (user != null && !user.isEmailVerified) {
                                    Toast.makeText(this, "Silakan verifikasi email Anda terlebih dahulu", Toast.LENGTH_SHORT).show()
                                    // Log out the user
                                    mAuth.signOut()
                                }
                            }
                            else -> {
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
