package org.d3if0140.masjidhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import org.d3if0140.masjidhub.databinding.ActivityUnggahBinding
import java.util.*

class UnggahActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnggahBinding
    private var selectedImageUri: Uri? = null
    private val storageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnggahBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    val intent = Intent(this, KeuanganActivity::class.java)
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
            if (selectedImageUri != null) {
                uploadImage()
            } else {
                submitPost(null)
            }
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
                .addOnFailureListener {
                    Toast.makeText(this, "Upload gagal", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun submitPost(imageUrl: String?) {
        val description = binding.editDesk.text.toString()
        if (description.isBlank()) {
            Toast.makeText(this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, EventActivity::class.java).apply {
            putExtra("description", description)
            putExtra("imageUrl", imageUrl)
        }
        startActivity(intent)
    }
}
