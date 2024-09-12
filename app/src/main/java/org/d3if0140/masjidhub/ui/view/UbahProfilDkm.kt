package org.d3if0140.masjidhub.ui.view

import android.app.Activity
import android.app.ProgressDialog
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.ActivityUbahProfilDkmBinding
import java.util.UUID

class UbahProfilDkm : AppCompatActivity() {
    private lateinit var binding: ActivityUbahProfilDkmBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mapsResultLauncher: ActivityResultLauncher<Intent>
    private val MAPS_REQUEST_CODE = 2
    private var latitude: Double? = null
    private var longitude: Double? = null
    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUbahProfilDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi mapsResultLauncher
        mapsResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val latitude = data?.getDoubleExtra("latitude", 0.0)
                val longitude = data?.getDoubleExtra("longitude", 0.0)
                if (latitude != null && longitude != null) {
                    this.latitude = latitude
                    this.longitude = longitude
                    binding.latitudeEditText.setText(latitude.toString())
                    binding.longitudeEditText.setText(longitude.toString())
                }
            }
        }


        // Load current profile data
        loadUserProfile()

        // Set onClickListener untuk tombol pilih lokasi
        binding.selectLocationButton.setOnClickListener {
            val intent = Intent(this, MapsUbahProfilActivity::class.java)
            mapsResultLauncher.launch(intent)
        }

        binding.buttonSimpan.setOnClickListener {
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Harap Tunggu...")
            progressDialog.show()

            val nama = binding.editTextNama.text.toString().trim()
            val alamat = binding.editTextAlamat.text.toString().trim()
            val lat = latitude
            val lon = longitude
            val nomor = binding.editTextNomor.text.toString().trim()
            val namaKetua = binding.editTextNamaKetua.text.toString().trim()
            val nomorKetua = binding.editTextNomorPengurus.text.toString().trim()


            if (nama.isNotEmpty()) {
                val currentUserId = mAuth.currentUser?.uid
                currentUserId?.let {
                    val updateData = mutableMapOf<String, Any>(
                        "nama" to nama
                    )
                    if (alamat.isNotEmpty()) updateData["alamat"] = alamat
                    if (nomor.isNotEmpty()) updateData["teleponMasjid"] = nomor
                    if (namaKetua.isNotEmpty()) updateData["namaKetua"] = namaKetua
                    if (nomorKetua.isNotEmpty()) updateData["teleponKetua"] = nomorKetua
                    lat?.let { updateData["latitude"] = it }
                    lon?.let { updateData["longitude"] = it }

                    firestore.collection("user")
                        .document(it)
                        .update(updateData)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Profil berhasil diubah", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, ProfilDkmActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            progressDialog.dismiss()
                            Toast.makeText(this, "Gagal mengubah profil: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "Nama harus diisi", Toast.LENGTH_SHORT).show()
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
                    val intent = Intent(this, LaporanKeuanganDkmActivity::class.java)
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
            uploadImage(imageUri)
        } else if (requestCode == MAPS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)
            if (latitude != null && longitude != null) {
                this.latitude = latitude
                this.longitude = longitude
                // Update UI or save to Firestore as needed
                binding.latitudeEditText.setText(latitude.toString())
                binding.longitudeEditText.setText(longitude.toString())
            }
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
                        val namaAtas = userData["nama"] as? String
                        val alamatAtas = userData["alamat"] as? String
                        val nama = userData["nama"] as? String
                        val alamat = userData["alamat"] as? String
                        val latitude = userData["latitude"] as? Double
                        val longitude = userData["longitude"] as? Double
                        val imageUrl = userData["imageUrl"] as? String
                        val nomor = userData["teleponMasjid"] as? String
                        val namaKetua = userData["namaKetua"] as? String
                        val nomorKetua = userData["teleponKetua"] as? String

                        // Update UI
                        namaAtas?.let { binding.namaUser.setText(it) }
                        alamatAtas?.let { binding.alamatMasjid.setText(it) }
                        nama?.let { binding.editTextNama.setText(it) }
                        alamat?.let { binding.editTextAlamat.setText(it) }
                        latitude?.let { this.latitude = it; binding.latitudeEditText.setText(it.toString()) }
                        longitude?.let { this.longitude = it; binding.longitudeEditText.setText(it.toString()) }
                        imageUrl?.let { loadProfileImage(it) } ?: run { displayDefaultProfileImage() }
                        nomor?.let { binding.editTextNomor.setText(it) }
                        namaKetua?.let { binding.editTextNamaKetua.setText(it) }
                        nomorKetua?.let { binding.editTextNomorPengurus.setText(it) }
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


