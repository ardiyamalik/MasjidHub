package org.d3if0140.masjidhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import org.d3if0140.masjidhub.databinding.ActivityUnggahBinding
import java.util.*

class UnggahActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnggahBinding
    private var selectedImageUri: Uri? = null
    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnggahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase App Check
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        binding.backButton.setOnClickListener{
            startActivity(Intent(this, DkmDashboard::class.java))
        }

        binding.bottomNavigationDkm.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.uploadEvent -> true
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
                R.id.menu_profile_dkm -> {
                    val intent = Intent(this, ProfilDkmActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        binding.insertImageButton.setOnClickListener {
            openFileChooser()
        }

        binding.submitButton.setOnClickListener {
            uploadImage()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.selectedImage.setImageURI(uri)
        } else {
            Toast.makeText(this, "Gambar tidak dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFileChooser() {
        pickImageLauncher.launch("image/*")
    }

    private fun uploadImage() {
        val fileName = UUID.randomUUID().toString()
        val imageRef = storageReference.child("images/$fileName")

        selectedImageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val imageUrl = downloadUri.toString()
                        submitPost(imageUrl)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("UnggahActivity", "Error uploading image", exception)
                    Toast.makeText(this, "Upload gagal", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun submitPost(imageUrl: String?) {
        val deskripsi = binding.editDesk.text.toString()
        if (deskripsi.isBlank()) {
            Toast.makeText(this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        val postData= hashMapOf(
            "deskripsi" to deskripsi,
            "imageUrl" to imageUrl,
            "userId" to currentUser.uid,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("posts")
            .add(postData)
            .addOnSuccessListener { documentReference ->
                val intent = Intent(this, EventActivity::class.java).apply {
                    putExtra("deskripsi", deskripsi)
                    putExtra("imageUrl", imageUrl)
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.e("UnggahActivity", "Error adding document", e)
                Toast.makeText(this, "Gagal menyimpan postingan", Toast.LENGTH_SHORT).show()
            }
    }
}
