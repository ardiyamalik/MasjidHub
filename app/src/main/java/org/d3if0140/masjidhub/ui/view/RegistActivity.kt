package org.d3if0140.masjidhub.ui.view

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.ActivityRegistBinding

class RegistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistBinding
    private lateinit var passwordToggle: ImageButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Handle back button click
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
        }

        // Make "Login" clickable
        val text = "Sudah punya akun? Login"
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf("Login")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                startActivity(Intent(this@RegistActivity, LoginActivity::class.java))
            }
        }
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            startIndex + 5,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textLogin.text = spannableString
        binding.textLogin.movementMethod = LinkMovementMethod.getInstance()

        // Handle password visibility toggle
        passwordToggle = binding.passwordToggle
        passwordToggle.setOnClickListener {
            val inputType = binding.passwordEditText.inputType
            if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                binding.passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off)
            } else {
                binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.baseline_visibility)
            }
            binding.passwordEditText.setSelection(binding.passwordEditText.text?.length ?: 0)
        }

        // Set up the Spinner for DKM options
        val spinner: Spinner = binding.DkmSpinner
        val dkmOptions = mutableListOf<String>()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dkmOptions)
        spinner.adapter = adapter

        // Fetch data from Firestore
        firestore.collection("user")
            .whereEqualTo("role", "pengurus_dkm")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Tidak ada data pengurus DKM", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
                        val namaDkm = document.getString("nama")
                        namaDkm?.let { dkmOptions.add(it) }
                    }
                    // Add "Masjid tidak ada" option
                    dkmOptions.add("Masjid tidak ada")
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                Log.e("FirestoreError", "Error getting documents", exception)
            }

        // Handle registration button click
        binding.registButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val nama = binding.namaEditText.text.toString()
            val dkm = spinner.selectedItem.toString()

            if (email.isEmpty() || password.isEmpty() || nama.isEmpty() || dkm.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field dan pilih masjid.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tampilkan loading
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Harap Tunggu...")
            progressDialog.show()

            // Register user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressDialog.dismiss() // Tutup loading setelah proses selesai
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = mAuth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(nama)
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    user.sendEmailVerification()
                                        .addOnCompleteListener { emailTask ->
                                            if (emailTask.isSuccessful) {
                                                Toast.makeText(
                                                    this,
                                                    "Email verifikasi telah dikirim",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                    val userData = hashMapOf(
                                        "nama" to nama,
                                        "email" to email,
                                        "dkm" to dkm,
                                        "role" to "jamaah"
                                    )

                                    firestore.collection("user")
                                        .document(user.uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            mAuth.signOut() // Sign out user after registration
                                            startActivity(Intent(this, KonfirmasiActivity::class.java))
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                this,
                                                "Gagal menyimpan data ke database",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                    } else {
                        if (task.exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(
                                this,
                                "Email sudah terdaftar. Silakan gunakan email lain.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(this, "Registrasi gagal. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }
}
