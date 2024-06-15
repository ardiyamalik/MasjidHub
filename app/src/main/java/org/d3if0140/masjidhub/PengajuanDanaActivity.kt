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
import org.d3if0140.masjidhub.databinding.ActivityPengajuanDanaBinding
import java.util.*

class PengajuanDanaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPengajuanDanaBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null
    private val TAG = "PengajuanDanaActivity"

    private val getImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.imageViewKTP.setImageURI(it)
            binding.imageViewKTP.visibility = android.view.View.VISIBLE
            imageUri = it
            Log.d(TAG, "Image selected: $imageUri")
        } ?: run {
            Log.d(TAG, "Image selection failed or canceled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengajuanDanaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonUploadKTP.setOnClickListener {
            getImageFromGallery.launch("image/*")
            Log.d(TAG, "Button 'Upload KTP' clicked")
        }

        binding.buttonSubmit.setOnClickListener {
            submitPengajuan()
            Log.d(TAG, "Button 'Submit' clicked")
        }
    }

    private fun submitPengajuan() {
        val nama = binding.editTextNama.text.toString()
        val jumlahStr = binding.editTextJumlah.text.toString()
        val alasan = binding.editTextAlasan.text.toString()
        val tanggal = binding.editTextTanggal.text.toString()

        if (nama.isEmpty() || jumlahStr.isEmpty() || alasan.isEmpty() || tanggal.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Field validation failed")
            return
        }

        val jumlah = jumlahStr.toDouble()
        val fileName = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("ktp/$fileName")

        Log.d(TAG, "Starting file upload for: $imageUri")
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
                    val userEmail = auth.currentUser?.email ?: "Unknown Email"
                    Log.d(TAG, "File upload successful, download URL: $uri")

                    val pengajuan = hashMapOf(
                        "nama" to nama,
                        "jumlah" to jumlah,
                        "alasan" to alasan,
                        "tanggal" to tanggal,
                        "ktpUrl" to uri.toString(),
                        "status" to "Pending",
                        "userEmail" to userEmail
                    )

                    db.collection("pengajuan_dana").add(pengajuan).addOnSuccessListener {
                        Toast.makeText(this, "Pengajuan berhasil diajukan", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Pengajuan data successfully submitted")
                        saveNotificationToFirestore(userEmail, jumlah)
                        val intent = Intent(this, NotificationDkmActivity::class.java)
                        startActivity(intent)
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal mengajukan dana: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Failed to submit pengajuan data: ${e.message}", e)
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal mendapatkan URL unduhan: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Failed to get download URL: ${e.message}", e)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengunggah foto KTP: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "File upload failed: ${e.message}", e)
            }
    }

    private fun saveNotificationToFirestore(userEmail: String, jumlah: Double) {
        val notificationData = hashMapOf(
            "title" to "Pengajuan Dana Baru",
            "message" to "Ada pengajuan dana baru dari $userEmail sebesar Rp $jumlah. Silakan cek aplikasi untuk detail lebih lanjut.",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("notifikasi_pengurus_dkm")
            .add(notificationData)
            .addOnSuccessListener {
                Log.d(TAG, "Notification saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save notification: ${e.message}", e)
            }
    }
}
