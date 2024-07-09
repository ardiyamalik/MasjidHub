package org.d3if0140.masjidhub

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.d3if0140.masjidhub.databinding.ActivityRegistDkmBinding

class RegisterDkmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistDkmBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var ktpUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firestore, Firebase Authentication, dan Firebase Storage
        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        Log.d("RegisterDkmActivity", "Firestore, FirebaseAuth, and FirebaseStorage initialized")

        // Set onClickListener untuk tombol register
        binding.registButton.setOnClickListener {
            Log.d("RegisterDkmActivity", "Register button clicked")
            registerMasjid()
        }

        // Set onClickListener untuk tombol upload KTP
        binding.uploadKtpButton.setOnClickListener {
            selectKtpImage()
        }
    }

    private fun selectKtpImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            ktpUri = data?.data
            binding.ktpImageView.setImageURI(ktpUri)
            Toast.makeText(this, "Foto KTP berhasil dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerMasjid() {
        // Ambil data dari form
        val nama = binding.namaMasjidEditText.text.toString()
        val alamat = binding.alamatEditText.text.toString()
        val kodePos = binding.kodePosEditText.text.toString()
        val teleponMasjid = binding.teleponMasjidEditText.text.toString()
        val namaKetua = binding.namaKetuaEditText.text.toString()
        val teleponKetua = binding.teleponKetuaEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        // Validasi data
        if (nama.isEmpty() || alamat.isEmpty() || kodePos.isEmpty() || teleponMasjid.isEmpty() ||
            namaKetua.isEmpty() || teleponKetua.isEmpty() || email.isEmpty() || password.isEmpty()
        ) {
            Log.w("RegisterDkmActivity", "Validation failed: Some fields are empty")
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        if (ktpUri == null) {
            Log.w("RegisterDkmActivity", "Validation failed: KTP image not selected")
            Toast.makeText(this, "Harap unggah foto KTP Ketua DKM", Toast.LENGTH_SHORT).show()
            return
        }

        // Register ke Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val user = mAuth.currentUser
                    if (user != null) {
                        val uid = user.uid

                        // Upload KTP to Firebase Storage
                        val ktpRef = storage.reference.child("ktp_ketua_dkm/$uid.jpg")
                        ktpRef.putFile(ktpUri!!)
                            .addOnSuccessListener {
                                ktpRef.downloadUrl.addOnSuccessListener { uri ->
                                    // Buat data masjid dengan URL KTP
                                    val masjidData = hashMapOf(
                                        "nama" to nama,
                                        "alamat" to alamat,
                                        "kodePos" to kodePos,
                                        "teleponMasjid" to teleponMasjid,
                                        "namaKetua" to namaKetua,
                                        "teleponKetua" to teleponKetua,
                                        "email" to email,
                                        "role" to "pengurus_dkm",
                                        "verified" to false,  // Awalnya belum terverifikasi
                                        "ktpKetuaUrl" to uri.toString()
                                    )

                                    Log.d("RegisterDkmActivity", "Data prepared for Firestore: $masjidData")

                                    // Simpan data ke Firestore dengan UID sebagai ID dokumen
                                    firestore.collection("user")
                                        .document(uid)
                                        .set(masjidData)
                                        .addOnSuccessListener {
                                            Log.i("RegisterDkmActivity", "Registration successful")
                                            Toast.makeText(
                                                this,
                                                "Registrasi berhasil, menunggu verifikasi admin",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                "RegisterDkmActivity",
                                                "Failed to save user data to Firestore",
                                                e
                                            )
                                            Toast.makeText(
                                                this,
                                                "Registrasi gagal: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("RegisterDkmActivity", "Failed to upload KTP image", e)
                                Toast.makeText(
                                    this,
                                    "Gagal mengunggah foto KTP: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Log.e("RegisterDkmActivity", "User is null after registration")
                        Toast.makeText(this, "Registrasi gagal: user null", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e(
                        "RegisterDkmActivity",
                        "User registration with FirebaseAuth failed",
                        authTask.exception
                    )
                    Toast.makeText(
                        this,
                        "Registrasi gagal: ${authTask.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
