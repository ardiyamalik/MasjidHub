package org.d3if0140.masjidhub.ui.view

import android.app.DatePickerDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityInputNeracaBinding
import java.util.Calendar

class InputNeracaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputNeracaBinding
    private lateinit var db: FirebaseFirestore
    private var tanggalLaporan: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputNeracaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Ambil userId dari Intent
        userId = intent.getStringExtra("userId")

        // Membuka dialog kalender saat editTextTanggal ditekan
        binding.editTextTanggal.setOnClickListener {
            showDatePicker { date ->
                tanggalLaporan = date
                binding.editTextTanggal.setText(date)
            }
        }

        // Saat tombol Submit ditekan
        binding.buttonSubmit.setOnClickListener {

            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Mengunggah...")
            progressDialog.show()

            val jumlahInfaq = binding.editTextJumlahInfaq.text.toString().toDoubleOrNull()
            val jumlahKas = binding.editTextJumlahKas.text.toString().toDoubleOrNull()
            val oprasionalMasjid = binding.OprasionalMasjid.text.toString().toDoubleOrNull() ?: 0.0
            val renov = binding.Renov.text.toString().toDoubleOrNull() ?: 0.0
            val kegiatan = binding.kegiatanSosial.text.toString().toDoubleOrNull() ?: 0.0
            val gaji = binding.GajiPengurus.text.toString().toDoubleOrNull() ?: 0.0

            if (validateInput(jumlahInfaq, jumlahKas)) {
                // Pastikan untuk tidak menggunakan nilai null
                submitLaporan(
                    jumlahInfaq ?: 0.0,
                    jumlahKas ?: 0.0,
                    oprasionalMasjid,
                    renov,
                    kegiatan,
                    gaji
                )
            } else {
                progressDialog.dismiss()
            }
        }

        // Tombol kembali
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, selectedDay)
            }

            if (selectedDate.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                val formattedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                onDateSelected(formattedDate)
            } else {
                Toast.makeText(this, "Silakan pilih hari Jumat untuk mulai laporan mingguan.", Toast.LENGTH_LONG).show()
            }

        }, year, month, day)

        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun validateInput(jumlahInfaq: Double?, jumlahKas: Double?): Boolean {
        return when {
            tanggalLaporan == null -> {
                Toast.makeText(this, "Silakan pilih tanggal laporan.", Toast.LENGTH_SHORT).show()
                false
            }
            jumlahInfaq == null -> {
                Toast.makeText(this, "Silakan masukkan jumlah infaq.", Toast.LENGTH_SHORT).show()
                false
            }
            jumlahKas == null -> {
                Toast.makeText(this, "Silakan masukkan jumlah kas.", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun submitLaporan(jumlahInfaq: Double, jumlahKas: Double, oprasionalMasjid: Double, renov: Double, kegiatan: Double, gaji: Double) {
        val laporan = hashMapOf(
            "tanggalLaporan" to tanggalLaporan,
            "jumlahInfaq" to jumlahInfaq,
            "jumlahKas" to jumlahKas,
            "oprasionalMasjid" to oprasionalMasjid,
            "renovasi" to renov,
            "kegiatanSosial" to kegiatan,
            "gajiPengurus" to gaji,
            "userId" to userId, // Tambahkan userId ke laporan
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("laporan_keuangan_masjid")
            .add(laporan)
            .addOnSuccessListener {
                Toast.makeText(this, "Laporan berhasil dikirim.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengirim laporan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}