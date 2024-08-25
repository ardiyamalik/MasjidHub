package org.d3if0140.masjidhub

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityInfaqBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class InfaqActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfaqBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val getImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.imageViewBuktiPembayaran.setImageURI(it)
            binding.imageViewBuktiPembayaran.visibility = android.view.View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengatur tanggal saat ini ke EditText tanggal
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        binding.editTextTanggal.setText(currentDate)

        binding.editTextTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        binding.editTextTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.buttonPilihFoto.setOnClickListener {
            getImageFromGallery.launch("image/*")
        }

        binding.buttonBayar.setOnClickListener {
            val jumlahInfaq = binding.editTextJumlahInfaq.text.toString().toDoubleOrNull()

            if (jumlahInfaq != null) {
                val currentUser = auth.currentUser
                val userId = currentUser?.uid ?: ""
                val userEmail = currentUser?.email ?: "Unknown"
                val tanggal = binding.editTextTanggal.text.toString()

                val infaqData = hashMapOf(
                    "userId" to userId,
                    "userEmail" to userEmail,
                    "jumlahInfaq" to jumlahInfaq,
                    "metodePembayaran" to "Bank Transfer",
                    "buktiPembayaran" to binding.imageViewBuktiPembayaran.drawable.toString(),
                    "tanggal" to tanggal,
                    "status" to "pending"
                )

                db.collection("infaq_masjid")
                    .add(infaqData)
                    .addOnSuccessListener {
                        saveNotificationToFirestore(jumlahInfaq)
                        Toast.makeText(this, "Infaq berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, KeuanganActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal menyimpan infaq: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Harap lengkapi form pembayaran", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNotificationToFirestore(jumlahInfaq: Double) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "unknown_user"

        val notificationData = hashMapOf(
            "userId" to userId,
            "title" to "Infaq sedang diproses",
            "message" to "Infaq sebesar Rp $jumlahInfaq sedang diproses oleh aplikasi.",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("notifikasi")
            .add(notificationData)
            .addOnSuccessListener {
                // Notifikasi berhasil disimpan
            }
            .addOnFailureListener { e ->
                // Gagal menyimpan notifikasi
                e.printStackTrace()
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
