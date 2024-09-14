package org.d3if0140.masjidhub.ui.view

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.d3if0140.masjidhub.databinding.ActivityRegistDkmBinding
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.security.MessageDigest

class RegisterDkmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistDkmBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var mapsResultLauncher: ActivityResultLauncher<Intent>
    private var ktpUri: Uri? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    companion object {
        private const val MAPS_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firestore, Firebase Authentication, dan Firebase Storage
        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        Log.d("RegisterDkmActivity", "Firestore, FirebaseAuth, and FirebaseStorage initialized")

        // Inisialisasi ActivityResultLauncher untuk MapsActivity
        mapsResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val latitude = result.data?.getDoubleExtra("latitude", 0.0)
                val longitude = result.data?.getDoubleExtra("longitude", 0.0)

                // Periksa apakah latitude dan longitude tidak null
                if (latitude != null && longitude != null) {
                    binding.latitudeEditText.setText(latitude.toString())
                    binding.longitudeEditText.setText(longitude.toString())
                } else {
                    Log.e("RegisterDkmActivity", "Latitude or longitude is null")
                }
            }
        }

        // Set onClickListener untuk tombol register
        binding.registButton.setOnClickListener {
            Log.d("RegisterDkmActivity", "Register button clicked")
            registerMasjid()
        }

        // Set onClickListener untuk tombol upload KTP
        binding.uploadKtpButton.setOnClickListener {
            selectKtpImage()
        }

        // Set onClickListener untuk tombol pilih lokasi
        binding.selectLocationButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            mapsResultLauncher.launch(intent)
        }

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
        }
    }

    private fun isKtpImageViewEmpty(): Boolean {
        return binding.ktpImageView.drawable == null
    }


    private fun selectKtpImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1000 -> {
                    ktpUri = data?.data
                    binding.ktpImageView.setImageURI(ktpUri)
                    ktpUri?.let { uri ->
                        contentResolver.openInputStream(uri)?.let { inputStream ->
                            val fileBytes = inputStream.readBytes()
                            val ktpHash = generateHash(fileBytes)

                            // Cek apakah hash sudah ada di Firestore
                            checkKtpHashInFirestore(ktpHash)
                        }
                    }
                }
            }
        }
    }

    private fun checkKtpHashInFirestore(ktpHash: String) {
        firestore.collection("user")
            .whereEqualTo("ktpHash", ktpHash)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Hash KTP sudah digunakan
                    Toast.makeText(this, "KTP ini sudah digunakan sebelumnya.", Toast.LENGTH_SHORT).show()
                    // Kosongkan ImageView jika KTP sudah digunakan
                    binding.ktpImageView.setImageURI(null)
                } else {
                    // Hash KTP belum digunakan, lanjutkan pendaftaran
                    Toast.makeText(this, "KTP valid, lanjutkan pendaftaran.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("RegisterDkmActivity", "Error checking KTP hash", e)
                Toast.makeText(this, "Gagal memeriksa KTP: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk membuat hash dari file byte
    private fun generateHash(fileBytes: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(fileBytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    // Tambahkan fungsi ini di luar onCreate untuk menghitung hash dari file URI
    private fun getFileHash(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val md = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                md.update(buffer, 0, bytesRead)
            }

            val digest = md.digest()
            val sb = StringBuilder()
            for (b in digest) {
                sb.append(String.format("%02x", b))
            }

            sb.toString()
        } catch (e: Exception) {
            Log.e("RegisterDkmActivity", "Error calculating hash", e)
            null
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
        val latitudeString = binding.latitudeEditText.text.toString()
        val longitudeString = binding.longitudeEditText.text.toString()

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Harap Tunggu...")
        progressDialog.show()

        // Validasi data
        if (nama.isEmpty() || alamat.isEmpty() || kodePos.isEmpty() || teleponMasjid.isEmpty() ||
            namaKetua.isEmpty() || teleponKetua.isEmpty() || email.isEmpty() || password.isEmpty() ||
            latitudeString.isEmpty() || longitudeString.isEmpty() || isKtpImageViewEmpty()
        ) {
            progressDialog.dismiss() // Tutup dialog jika ada field kosong
            Log.w("RegisterDkmActivity", "Validation failed: Some fields are empty or location is not set")
            Toast.makeText(this, "Harap isi semua field dan pilih lokasi", Toast.LENGTH_SHORT).show()
            return
        }

        // Parsing latitude dan longitude dari String ke Double
        val latitude = latitudeString.toDoubleOrNull()
        val longitude = longitudeString.toDoubleOrNull()

        if (ktpUri == null) {
            progressDialog.dismiss() // Tutup dialog jika ada field kosong
            Log.w("RegisterDkmActivity", "Validation failed: KTP image not selected")
            Toast.makeText(this, "Harap unggah foto KTP Ketua DKM", Toast.LENGTH_SHORT).show()
            return
        }

        // Hitung hash dari file KTP
        val ktpHash = getFileHash(ktpUri!!)
        if (ktpHash == null) {
            progressDialog.dismiss()
            Toast.makeText(this, "Gagal menghitung hash KTP", Toast.LENGTH_SHORT).show()
            return
        }

        // Register ke Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                progressDialog.dismiss()
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
                                        "ktpKetuaUrl" to uri.toString(),
                                        "ktpHash" to ktpHash,  // Simpan hash KTP
                                        "password" to password,
                                        "latitude" to (latitude ?: 0.0),
                                        "longitude" to (longitude ?: 0.0)
                                    )

                                    // Simpan data ke Firestore dengan UID sebagai ID dokumen
                                    firestore.collection("user")
                                        .document(uid)
                                        .set(masjidData)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this,
                                                "Registrasi berhasil, menunggu verifikasi admin",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            mAuth.signOut()
                                            startActivity(Intent(this, KonfirmasiActivity::class.java))
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("RegisterDkmActivity", "Failed to save user data to Firestore", e)
                                            Toast.makeText(this, "Registrasi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("RegisterDkmActivity", "Failed to upload KTP image", e)
                                Toast.makeText(this, "Gagal mengunggah foto KTP: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Registrasi gagal: user null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Registrasi gagal: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
