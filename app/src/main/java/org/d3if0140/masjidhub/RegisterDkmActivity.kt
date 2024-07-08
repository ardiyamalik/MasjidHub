package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityRegistDkmBinding

class RegisterDkmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistDkmBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firestore
        firestore = FirebaseFirestore.getInstance()
        Log.d("RegisterDkmActivity", "Firestore initialized")

        // Set onClickListener untuk tombol register
        binding.registButton.setOnClickListener {
            Log.d("RegisterDkmActivity", "Register button clicked")
            registerMasjid()
        }
    }

    private fun registerMasjid() {
        // Ambil data dari form
        val namaMasjid = binding.namaMasjidEditText.text.toString()
        val alamat = binding.alamatEditText.text.toString()
        val kodePos = binding.kodePosEditText.text.toString()
        val teleponMasjid = binding.teleponMasjidEditText.text.toString()
        val namaKetua = binding.namaKetuaEditText.text.toString()
        val teleponKetua = binding.teleponKetuaEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password= binding.passwordEditText.text.toString()

        // Validasi data
        if (namaMasjid.isEmpty() || alamat.isEmpty() || kodePos.isEmpty() || teleponMasjid.isEmpty() ||
            namaKetua.isEmpty() || teleponKetua.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Log.w("RegisterDkmActivity", "Validation failed: Some fields are empty")
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        // Buat data masjid
        val masjidData = hashMapOf(
            "namaMasjid" to namaMasjid,
            "alamat" to alamat,
            "kodePos" to kodePos,
            "teleponMasjid" to teleponMasjid,
            "namaKetua" to namaKetua,
            "teleponKetua" to teleponKetua,
            "email" to email,
            "password" to password,
            "role" to "pengurus_dkm",
            "verified" to false  // Awalnya belum terverifikasi
        )

        Log.d("RegisterDkmActivity", "Data prepared for Firestore: $masjidData")

        // Simpan data ke Firestore
        firestore.collection("user")
            .add(masjidData)
            .addOnSuccessListener {
                Log.i("RegisterDkmActivity", "Registration successful")
                Toast.makeText(this, "Registrasi berhasil, menunggu verifikasi admin", Toast.LENGTH_SHORT).show()
                // Kirim data ke halaman regist dkm admin (logika ini tergantung pada implementasi aplikasi Anda)
                sendForVerification()
            }
            .addOnFailureListener { e ->
                Log.e("RegisterDkmActivity", "Registration failed", e)
                Toast.makeText(this, "Registrasi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendForVerification() {
        // Logika untuk mengirim data ke halaman regist dkm admin
        // Misalnya, Anda bisa menggunakan intent untuk berpindah ke activity admin
        Log.d("RegisterDkmActivity", "Sending for verification")
        val intent = Intent(this, KonfirmasiActivity::class.java)
        startActivity(intent)
    }
}
