package org.d3if0140.masjidhub

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.VolleyLog.TAG
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.d3if0140.masjidhub.databinding.ActivityUploadDataInfaqBinding
import java.io.InputStream

class UploadDataInfaq : AppCompatActivity() {

    private lateinit var binding: ActivityUploadDataInfaqBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadDataInfaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        // Set click listener for image selection
        binding.selectImageButton.setOnClickListener {
            openImageChooser()
        }

        // Set click listener for upload button
        binding.uploadButton.setOnClickListener {
            uploadData()
        }

        binding.backButton.setOnClickListener{
            startActivity(Intent(this, ProfilDkmActivity::class.java))
        }

        // Fetch user data and populate fields
        fetchUserData()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.d(TAG, "User ID is null")
            return
        }

        Log.d(TAG, "Fetching user data for user ID: $userId")

        db.collection("user").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val nomorRekening = document.getString("nomorRekening")
                    val namaRekening = document.getString("namaRekening")
                    val namaBank = document.getString("namaBank")
                    val qrImageUrl = document.getString("qrImageUrl")

                    Log.d(TAG, "User data fetched successfully")

                    binding.norekEditText.setText(nomorRekening ?: "")
                    binding.namaRekening.setText(namaRekening ?: "")
                    binding.namaBank.setText(namaBank ?: "")
                    // Load QR image using Glide
                    qrImageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .into(binding.qrImageView)
                    }


                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting user data: ${e.message}", e)
            }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.qrImageView.setImageBitmap(bitmap)
        }
    }

    private fun uploadData() {
        val norek = binding.norekEditText.text.toString()
        val namaRekening = binding.namaRekening.text.toString()
        val namaBank = binding.namaBank.text.toString()

        if (imageUri == null || norek.isEmpty() || namaRekening.isEmpty() || namaBank.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading...")
        progressDialog.show()

        // Upload image
        val imageRef = storageReference.child("qr_images/${System.currentTimeMillis()}.jpg")
        imageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    saveDataToFirestore(norek, namaRekening, namaBank, imageUrl)
                    progressDialog.dismiss()
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveDataToFirestore(norek: String, namaRekening: String, namaBank: String, qrImageUrl: String) {
        val currentUserId = mAuth.currentUser?.uid
        if (currentUserId != null) {
            val userMap = mapOf(
                "nomorRekening" to norek,
                "namaRekening" to namaRekening,
                "namaBank" to namaBank,
                "qrImageUrl" to qrImageUrl
            ) as MutableMap<String, Any> // Explicit cast to MutableMap<String, Any>

            firestore.collection("user")
                .document(currentUserId)
                .update(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data uploaded successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
