package org.d3if0140.masjidhub.ui.view

import android.app.DatePickerDialog
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputNeracaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Membuka dialog kalender saat editTextTanggal ditekan
        binding.editTextTanggal.setOnClickListener {
            showDatePicker { date ->
                tanggalLaporan = date
                binding.editTextTanggal.setText(date)
            }
        }

        // Saat tombol Submit ditekan
        binding.buttonSubmit.setOnClickListener {
            val jumlahInfaq = binding.editTextJumlahInfaq.text.toString().toDoubleOrNull()
            val jumlahKas = binding.editTextJumlahKas.text.toString().toDoubleOrNull()
            val jumlahPengeluaran = binding.editTextJumlahPengeluaran.text.toString().toDoubleOrNull()
            val oprasionalMasjid = binding.OprasionalMasjid.text.toString().toDoubleOrNull()
            val renov = binding.Renov.text.toString().toDoubleOrNull()
            val kegiatan = binding.kegiatanSosial.text.toString().toDoubleOrNull()
            val gaji = binding.GajiPengurus.text.toString().toDoubleOrNull()

            if (validateInput(jumlahInfaq, jumlahKas, jumlahPengeluaran, oprasionalMasjid, renov, kegiatan, gaji)) {
                submitLaporan(jumlahInfaq!!, jumlahKas!!, jumlahPengeluaran!!, oprasionalMasjid!!, renov!!, kegiatan!!, gaji!!)
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

    private fun validateInput(jumlahInfaq: Double?, jumlahKas: Double?, jumlahPengeluaran: Double?, oprasionalMasjid: Double?, renov: Double?, kegiatan: Double?, gaji: Double?): Boolean {
        return when {
            tanggalLaporan == null -> {
                Toast.makeText(this, "Silakan pilih tanggal laporan.", Toast.LENGTH_SHORT).show()
                false
            }
            jumlahInfaq == null || jumlahKas == null || jumlahPengeluaran == null || oprasionalMasjid == null || renov == null || kegiatan == null || gaji == null -> {
                Toast.makeText(this, "Silakan masukkan semua jumlah.", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun submitLaporan(jumlahInfaq: Double, jumlahKas: Double, jumlahPengeluaran: Double, oprasionalMasjid: Double, renov: Double, kegiatan: Double, gaji: Double) {
        val laporan = hashMapOf(
            "tanggalLaporan" to tanggalLaporan,
            "jumlahInfaq" to jumlahInfaq,
            "jumlahKas" to jumlahKas,
            "jumlahPengeluaran" to jumlahPengeluaran,
            "oprasionalMasjid" to oprasionalMasjid,
            "renovasi" to renov,
            "kegiatanSosial" to kegiatan,
            "gajiPengurus" to gaji,
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
