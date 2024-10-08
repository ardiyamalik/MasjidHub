package org.d3if0140.masjidhub.ui.view

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.ActivityUnggahBinding
import java.text.SimpleDateFormat
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

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, DkmDashboard::class.java))
        }

        binding.bottomNavigationDkm.selectedItemId = R.id.uploadEvent
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
                    val intent = Intent(this, LaporanKeuanganDkmActivity::class.java)
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

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        binding.editTanggal.setText(currentDate)

        binding.editTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        binding.insertImageButton.setOnClickListener {
            openFileChooser()
        }

        binding.submitButton.setOnClickListener {
            uploadImage()
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mengunggah...")
        progressDialog.show()

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

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }


    private fun submitPost(imageUrl: String?) {
        val namaEvent = binding.editNamaEvent.text.toString()
        val tanggalEvent = binding.editTanggal.text.toString()
        val lokasiEvent = binding.editLokasi.text.toString()
        val linkEvent = binding.editLink.text.toString()
        val deskripsi = binding.editDesk.text.toString()
        if (namaEvent.isBlank() || tanggalEvent.isBlank() || lokasiEvent.isBlank() ||linkEvent.isBlank()||deskripsi.isBlank()) {
            Toast.makeText(this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        val timestamp = System.currentTimeMillis()
        val postData = hashMapOf(
            "namaEvent" to namaEvent,
            "tanggalEvent" to tanggalEvent,
            "lokasiEvent" to lokasiEvent,
            "linkEvent" to linkEvent,
            "deskripsi" to deskripsi,
            "imageUrl" to imageUrl,
            "userId" to currentUser.uid,
            "timestamp" to timestamp,
            "formattedDate" to formatTimestamp(timestamp) // Menambahkan tanggal dan jam yang diformat
        )

        firestore.collection("posts")
            .add(postData)
            .addOnSuccessListener { documentReference ->
                val intent = Intent(this, EventActivity::class.java).apply {
                    putExtra("namaEvent", namaEvent)
                    putExtra("tanggalEvent", tanggalEvent)
                    putExtra("lokasiEvent", lokasiEvent)
                    putExtra("linkEvent", linkEvent)
                    putExtra("deskripsi", deskripsi)
                    putExtra("imageUrl", imageUrl)
                    putExtra("timestamp", timestamp) // Kirim timestamp ke Activity berikutnya
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.e("UnggahActivity", "Error adding document", e)
                Toast.makeText(this, "Gagal menyimpan postingan", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.editTanggal.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}
