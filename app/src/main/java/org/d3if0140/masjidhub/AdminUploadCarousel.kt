package org.d3if0140.masjidhub

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityAdminUploadCarouselBinding
import java.util.*

class AdminUploadCarousel : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUploadCarouselBinding
    private val imageUriList = mutableListOf<Uri>()
    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUploadCarouselBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView untuk menampilkan preview gambar
        imageAdapter = ImageAdapter(imageUriList)
        binding.recyclerViewPreview.apply {
            layoutManager = LinearLayoutManager(this@AdminUploadCarousel, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }

        // Event listener untuk memilih gambar
        binding.buttonSelectImages.setOnClickListener {
            selectImagesFromGallery()
        }

        // Event listener untuk mengupload semua gambar setelah preview
        binding.buttonUploadImages.setOnClickListener {
            if (imageUriList.isNotEmpty()) {
                uploadAllImagesToFirebase()
            }
        }

        // Menambahkan onClickListener untuk button backButton
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
        }
    }

    // Mengambil beberapa gambar dari galeri
    private val selectImagesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data?.clipData != null) {
                val clipData = result.data!!.clipData

                // Mengambil semua gambar yang dipilih
                for (i in 0 until clipData!!.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri
                    imageUriList.add(imageUri)
                }

                imageAdapter.notifyDataSetChanged() // Refresh RecyclerView dengan gambar baru
                binding.buttonUploadImages.isEnabled = true // Aktifkan tombol upload
            } else if (result.data?.data != null) { // Jika hanya satu gambar dipilih
                imageUriList.add(result.data!!.data!!)
                imageAdapter.notifyDataSetChanged()
                binding.buttonUploadImages.isEnabled = true
            }
        }

    // Fungsi untuk memilih beberapa gambar dari galeri
    private fun selectImagesFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Memungkinkan memilih beberapa gambar
        }
        selectImagesLauncher.launch(intent)
    }

    // Fungsi untuk mengupload semua gambar ke Firebase Storage
    private fun uploadAllImagesToFirebase() {
        for (imageUri in imageUriList) {
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val ref = storageReference.child("carousel/$fileName")

            ref.putFile(imageUri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        saveImageToFirestore(uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Upload gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Menyimpan URL gambar ke Firestore
    private fun saveImageToFirestore(imageUrl: String) {
        val carouselData = hashMapOf(
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("carousel").add(carouselData)
            .addOnSuccessListener {
                Toast.makeText(this, "Gambar berhasil diupload", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
