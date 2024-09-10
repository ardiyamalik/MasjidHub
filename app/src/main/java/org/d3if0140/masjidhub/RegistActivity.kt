package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityRegistBinding

class RegistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistBinding
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordToggle: ImageButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        val text = "Sudah punya akun? Login"
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf("Login")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val intent = Intent(this@RegistActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            startIndex + 4,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textLogin.text = spannableString
        binding.textLogin.movementMethod = LinkMovementMethod.getInstance()

        passwordEditText = binding.passwordEditText
        passwordToggle = binding.passwordToggle

        passwordToggle.setOnClickListener {
            val inputType = passwordEditText.inputType
            if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off)
            } else {
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.baseline_visibility)
            }
            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
        }

        val emailEditText: TextInputEditText = binding.emailEditText
        val spinner: Spinner = binding.DkmSpinner
        val namaEditText: TextInputEditText = binding.namaEditText
        val passwordEditText: TextInputEditText = binding.passwordEditText

        // Inisialisasi adapter spinner
        val dkmOptions = mutableListOf<String>()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dkmOptions)
        spinner.adapter = adapter

        // Ambil data dari Firestore
        firestore.collection("user")
            .whereEqualTo("role", "pengurus_dkm")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("Firestore", "No documents found")
                    Toast.makeText(this, "Tidak ada data pengurus_dkm yang ditemukan", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
                        val namaDkm = document.getString("nama")
                        namaDkm?.let { dkmOptions.add(it) }
                    }
                    // Tambahkan opsi "Masjid tidak ada" di akhir daftar
                    dkmOptions.add("Masjid tidak ada")
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mengambil data dari Firestore", Toast.LENGTH_SHORT).show()
                Log.d("FirestoreError", "Error getting documents: ", exception)
            }


        binding.registButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val nama = namaEditText.text.toString()
            val dkm = spinner.selectedItem.toString()

            if (email.isEmpty() || password.isEmpty() || nama.isEmpty() || dkm.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field dan pilih masjid.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = mAuth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(nama)
                            .build()

                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Email verifikasi telah dikirim",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Gagal mengirim email verifikasi",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        val userData = hashMapOf(
                            "nama" to nama,
                            "email" to email,
                            "dkm" to dkm,
                            "password" to password,
                            "role" to "jamaah"
                        )
                        firestore.collection("user")
                            .document(user!!.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                // Hapus sesi pengguna jika ada
                                mAuth.signOut()
                                val intent = Intent(this, KonfirmasiActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Gagal menyimpan data ke database",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        if (task.exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(
                                this,
                                "Email telah digunakan untuk registrasi. Silakan gunakan email lain.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Registrasi gagal, silakan coba lagi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }
    }
}
