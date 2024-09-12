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
import org.d3if0140.masjidhub.databinding.ActivityPengajuanDanaBinding
import java.text.SimpleDateFormat
import java.util.*

class PengajuanDanaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPengajuanDanaBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var ktpUri: Uri? = null
    private var buktiUri: Uri? = null
    private val TAG = "PengajuanDanaActivity"

    private val getKtpFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.imageViewKTP.setImageURI(it)
            binding.imageViewKTP.visibility = android.view.View.VISIBLE
            ktpUri = it
            Log.d(TAG, "KTP Image selected: $ktpUri")
        } ?: run {
            Log.d(TAG, "KTP Image selection failed or canceled")
        }
    }

    private val getBuktiFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.imageViewFotoPendukung.setImageURI(it)
            binding.imageViewFotoPendukung.visibility = android.view.View.VISIBLE
            buktiUri = it
            Log.d(TAG, "Bukti Image selected: $buktiUri")
        } ?: run {
            Log.d(TAG, "Bukti Image selection failed or canceled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengajuanDanaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        binding.editTextTanggal.setText(currentDate)

        binding.editTextTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        binding.buttonUploadKTP.setOnClickListener {
            getKtpFromGallery.launch("image/*")
            Log.d(TAG, "Button 'Upload KTP' clicked")
        }

        binding.buttonUploadFotoPendukung.setOnClickListener {
            getBuktiFromGallery.launch("image/*")
            Log.d(TAG, "Button 'Upload Foto Bukti Pendukung' clicked")
        }

        binding.buttonSubmit.setOnClickListener {
            submitPengajuan()
            Log.d(TAG, "Button 'Submit' clicked")
        }

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, DkmDashboard::class.java))
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
                    val namaMasjid = document.getString("nama")
                    val lokasi = document.getString("alamat")
                    val teleponKetua = document.getString("teleponKetua")
                    val namaBank = document.getString("namaBank")
                    val nomorRekening = document.getString("nomorRekening")
                    val namaRekening = document.getString("namaRekening")

                    Log.d(TAG, "User data fetched successfully: nama=$namaMasjid, teleponKetua=$teleponKetua")

                    binding.editTextNama.setText(namaMasjid ?: "")
                    binding.editTextLokasi.setText(lokasi ?: "")
                    binding.editTextKontak.setText(teleponKetua ?: "")
                    binding.namaBank.setText(namaBank ?: "")
                    binding.namaRekening.setText(namaRekening ?: "")
                    binding.nomorRekening.setText(nomorRekening ?: "")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting user data: ${e.message}", e)
            }
    }

    private fun submitPengajuan() {
        val nama = binding.editTextNama.text.toString()
        val jumlahStr = binding.editTextJumlah.text.toString()
        val alasan = binding.editTextAlasan.text.toString()
        val tanggal = binding.editTextTanggal.text.toString()
        val kontak = binding.editTextKontak.text.toString()
        val lokasi = binding.editTextLokasi.text.toString()
        val namaBank = binding.namaBank.text.toString()
        val nomorRekening = binding.nomorRekening.text.toString()
        val namaRekening = binding.namaRekening.text.toString()

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Harap Tunggu...")
        progressDialog.show()

        if (nama.isEmpty() || jumlahStr.isEmpty() || alasan.isEmpty() || tanggal.isEmpty() ||
            kontak.isEmpty() || lokasi.isEmpty() || namaBank.isEmpty() || nomorRekening.isEmpty() ||
            namaRekening.isEmpty() || ktpUri == null || buktiUri == null) {
            progressDialog.dismiss() // Tutup dialog jika ada field kosong
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Field validation failed")
            return
        }

        val jumlah = jumlahStr.toDouble()
        val ktpFileName = UUID.randomUUID().toString()
        val buktiFileName = UUID.randomUUID().toString()
        val ktpStorageRef = storage.reference.child("ktp/$ktpFileName")
        val buktiStorageRef = storage.reference.child("bukti/$buktiFileName")

        Log.d(TAG, "Starting file upload for: $ktpUri and $buktiUri")

        // Upload KTP
        ktpStorageRef.putFile(ktpUri!!)
            .addOnSuccessListener {
                ktpStorageRef.downloadUrl.addOnSuccessListener { ktpUri ->
                    progressDialog.dismiss() // Tutup dialog jika ada field kosong
                    Log.d(TAG, "KTP file upload successful, download URL: $ktpUri")

                    // Upload Bukti
                    buktiStorageRef.putFile(buktiUri!!)
                        .addOnSuccessListener {
                            buktiStorageRef.downloadUrl.addOnSuccessListener { buktiUri ->
                                progressDialog.dismiss() // Tutup dialog jika ada field kosong
                                Log.d(TAG, "Bukti file upload successful, download URL: $buktiUri")

                                val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
                                val userEmail = auth.currentUser?.email ?: "Unknown Email"

                                val pengajuan = hashMapOf(
                                    "nama" to nama,
                                    "userId" to userId,
                                    "jumlah" to jumlah,
                                    "alasan" to alasan,
                                    "tanggal" to tanggal,
                                    "kontak" to kontak,
                                    "lokasi" to lokasi,
                                    "ktpUrl" to ktpUri.toString(),
                                    "buktiUrl" to buktiUri.toString(),
                                    "status" to "Pending",
                                    "userEmail" to userEmail,
                                    "namaBank" to namaBank,
                                    "nomorRekening" to nomorRekening,
                                    "namaRekening" to namaRekening,
                                    "tipe" to "pengajuan_dana"
                                )

                                db.collection("transaksi_keuangan").add(pengajuan).addOnSuccessListener {
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
                            Toast.makeText(this, "Gagal mengunggah foto bukti: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "File upload failed: ${e.message}", e)
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
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "unknown_user" // Menyimpan userId dari pengguna saat ini

        // Format jumlah tanpa desimal
        val formattedJumlah = jumlah.toInt().toString()

        val notificationData = hashMapOf(
            "userId" to userId, // Tambahkan userId ke data notifikasi
            "title" to "Pengajuan Dana Baru",
            "message" to "Pengajuan sebesar Rp $formattedJumlah sedang diproses oleh admin",
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

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.editTextTanggal.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}
