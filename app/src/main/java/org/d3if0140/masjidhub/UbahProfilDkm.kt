package org.d3if0140.masjidhub

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.d3if0140.masjidhub.databinding.ActivityUbahProfilBinding
import org.d3if0140.masjidhub.databinding.ActivityUbahProfilDkmBinding
import java.util.UUID

class UbahProfilDkm : AppCompatActivity() {
    private lateinit var binding: ActivityUbahProfilDkmBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUbahProfilDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Load current profile data
        loadUserProfile()

        binding.buttonSimpan.setOnClickListener {
            val nama = binding.editTextNama.text.toString().trim()
            val alamat = binding.editTextAlamat.text.toString().trim()

            if (nama.isNotEmpty()) { // Validasi jika nama tidak kosong
                val currentUserId = mAuth.currentUser?.uid
                currentUserId?.let {
                    firestore.collection("user")
                        .document(it)
                        .update(mapOf(
                            "nama" to nama,
                            "alamat" to alamat
                        ))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profil berhasil diubah", Toast.LENGTH_SHORT).show()
                            // Arahkan ke ProfilActivity
                            val intent = Intent(this, ProfilDkmActivity::class.java)
                            startActivity(intent)
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

        binding.backButton.setOnClickListener{
            val intent = Intent(this, ProfilDkmActivity::class.java)
            startActivity(intent)
        }

        // Bottom navigation listener
        binding.bottomNavigationDkm.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile_dkm -> true
                R.id.menu_home_dkm -> {
                    val intent = Intent(this, DkmDashboard::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_finance -> {
                    val intent = Intent(this, KeuanganDkmActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.unggahEvent -> {
                    val intent = Intent(this, UnggahActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
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
                .update(mapOf("imageUrl" to imageUrl)) // Menggunakan update untuk mempertahankan data yang ada
                .addOnSuccessListener {
                    Toast.makeText(this, "Foto profil berhasil diubah", Toast.LENGTH_SHORT).show()
                    // Update UI with new image
                    loadProfileImage(imageUrl)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal mengubah foto profil: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadUserProfile() {
        val currentUserId = mAuth.currentUser?.uid
        currentUserId?.let {
            firestore.collection("user").document(it).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userData = document.data
                    if (userData != null) {
                        val nama = userData["nama"] as? String
                        val dkm = userData["alamat"] as? String
                        val imageUrl = userData["imageUrl"] as? String

                        // Update UI
                        nama?.let { binding.namaUser.text = it }
                        dkm?.let { binding.alamatMasjid.text = it }
                        imageUrl?.let { loadProfileImage(it) } ?: run { displayDefaultProfileImage() }
                    }
                }
            }
        }
    }

    private fun loadProfileImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(createAvatar(mAuth.currentUser?.email ?: ""))
            .into(binding.profileImageView)
    }

    private fun displayDefaultProfileImage() {
        val user = mAuth.currentUser
        if (user != null && user.email != null) {
            val email = user.email
            email?.let {
                val avatarDrawable = createAvatar(it)
                binding.profileImageView.setImageDrawable(avatarDrawable)
            }
        }
    }

    private fun createAvatar(email: String): Drawable {
        val color = getColorFromEmail(email)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { this.color = color }
        canvas.drawCircle(50f, 50f, 50f, paint)
        val textPaint = Paint().apply {
            this.color = Color.WHITE
            this.textSize = 40f
            this.textAlign = Paint.Align.CENTER
        }
        val initial = email.substring(0, 1).uppercase()
        canvas.drawText(initial, 50f, 65f, textPaint)
        return BitmapDrawable(resources, bitmap)
    }

    private fun getColorFromEmail(email: String): Int {
        val hashCode = email.hashCode()
        return Color.HSVToColor(floatArrayOf(
            (hashCode and 0xFF).toFloat() % 360,
            0.6f,
            0.9f
        ))
    }
}


