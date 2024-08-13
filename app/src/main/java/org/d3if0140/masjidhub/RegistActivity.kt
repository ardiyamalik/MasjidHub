package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
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
    // Deklarasi variabel binding untuk menggunakan ViewBinding
    private lateinit var binding: ActivityRegistBinding
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordToggle: ImageButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore // Tambahkan deklarasi variabel firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menggunakan inflate pada binding untuk menghubungkan layout XML dengan Activity
        binding = ActivityRegistBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Inisialisasi Firebase App Check
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Inisialisasi Firebase
        //FirebaseApp.initializeApp(this)

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance()

        // Initialize Firestore instance
        firestore = FirebaseFirestore.getInstance()

        // Menambahkan onClickListener pada button backButton untuk kembali ke WelcomeActivity
        binding.backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        // Set teks pada textDaftar menjadi "Belum punya akun? Daftar" dengan tautan ke RegistActivity
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


        // Menghubungkan passwordEditText dan passwordToggle dengan elemen UI yang sesuai
        passwordEditText = binding.passwordEditText
        passwordToggle = binding.passwordToggle

        // Menambahkan onClickListener pada passwordToggle untuk mengubah visibilitas password
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

        // Menghubungkan emailEditText dan spinner dengan elemen UI yang sesuai
        val emailEditText: TextInputEditText = binding.emailEditText
        val spinner: Spinner = binding.DkmSpinner
        val namaEditText: TextInputEditText = binding.namaEditText
        val passwordEditText: TextInputEditText = binding.passwordEditText

        // Data untuk opsi dropdown pada spinner
        val dkmOptions = arrayOf(
            "Jamaah Masjid",
            "Masjid Nurul Hikmah",
            "Masjid Al-Lathif",
            "Masjid Al-Jabar"
        )

        // Membuat adapter untuk spinner dan mengatur posisi awal ke "Jamaah Masjid"
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dkmOptions)
        spinner.adapter = adapter
        spinner.setSelection(0)

        // Menambahkan onClickListener pada button register
        binding.registButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val nama = namaEditText.text.toString()
            val dkm = spinner.selectedItem.toString()

            // Melakukan proses registrasi dengan FirebaseAuth
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registrasi berhasil
                        val user: FirebaseUser? = mAuth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(nama)
                            .build()

                        // Mengirim email verifikasi
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

                        // Menyimpan data ke Firestore
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
                                // Data berhasil disimpan ke Firestore
                                val intent = Intent(this, KonfirmasiActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                // Gagal menyimpan data ke Firestore
                                Toast.makeText(
                                    this,
                                    "Gagal menyimpan data ke database",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                    } else {
                        // Registrasi gagal
                        // Cek apakah registrasi gagal karena email sudah digunakan sebelumnya
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
