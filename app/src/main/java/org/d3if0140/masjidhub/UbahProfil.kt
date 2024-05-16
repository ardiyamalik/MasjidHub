package org.d3if0140.masjidhub

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.d3if0140.masjidhub.databinding.ActivityUbahProfilBinding
import java.util.*

class UbahProfil : AppCompatActivity() {
    private lateinit var binding: ActivityUbahProfilBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUbahProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
        binding.DkmSpinnerUbah.adapter = adapter
        binding.DkmSpinnerUbah.setSelection(0)

        binding.buttonSimpan.setOnClickListener {
            val nama = binding.editTextNama.text.toString().trim()
            val dkm = binding.DkmSpinnerUbah.selectedItem.toString() // Ambil nilai yang dipilih dari spinner

            if (nama.isNotEmpty()) { // Hapus validasi untuk alamat email
                val currentUserId = mAuth.currentUser?.uid
                currentUserId?.let {
                    firestore.collection("user")
                        .document(it)
                        .update(mapOf(
                            "nama" to nama,
                            "dkm" to dkm
                            // tambahkan bidang lainnya yang ingin diubah
                        ))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profil berhasil diubah", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Gagal mengubah profil: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Mohon isi semua bidang dengan benar", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonUbahFotoProfil.setOnClickListener {
            // Panggil intent untuk memilih gambar dari galeri
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            // Unggah gambar profil ke Firebase Storage
            uploadImage(imageUri)
        }
    }

    private fun uploadImage(imageUri: Uri?) {
        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
            val uploadTask = storageRef.putFile(imageUri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    // Simpan URL gambar di Firestore
                    saveImageUrlToFirestore(downloadUri.toString())
                } else {
                    // Handle failures
                    Toast.makeText(this, "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val currentUserId = mAuth.currentUser?.uid
        currentUserId?.let {
            firestore.collection("user")
                .document(it)
                .set(mapOf("imageUrl" to imageUrl))
                .addOnSuccessListener {
                    Toast.makeText(this, "Foto profil berhasil diubah", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal mengubah foto profil: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
