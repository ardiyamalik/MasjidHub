package org.d3if0140.masjidhub.ui.view

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.ActivityInputNeracaBinding
import java.util.Calendar

class InputNeracaActivity : AppCompatActivity() {
    private lateinit var binding:ActivityInputNeracaBinding
    private lateinit var db: FirebaseFirestore
    private var tanggalMulai: String? = null
    private var tanggalSelesai: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputNeracaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Membuka dialog kalender saat editTextTanggalMulai ditekan
        binding.editTextTanggalMulai.setOnClickListener {
            showDatePicker { date ->
                tanggalMulai = date
                binding.editTextTanggalMulai.setText(date)
            }
        }

        // Membuka dialog kalender saat editTextTanggalSelesai ditekan
        binding.editTextTanggalSelesai.setOnClickListener {
            showDatePicker { date ->
                tanggalSelesai = date
                binding.editTextTanggalSelesai.setText(date)
            }
        }

        // Saat tombol Submit ditekan
        binding.buttonSubmit.setOnClickListener {
            val jumlahInfaq = binding.editTextJumlahInfaq.text.toString()
            val jumlahKas = binding.editTextJumlahKas.text.toString()
            val jumlahPengeluaran = binding.editTextJumlahPengeluaran.text.toString()

            // Validasi input
            if (validateInput(jumlahInfaq, jumlahKas, jumlahPengeluaran)) {
                // Simpan data ke Firestore
                saveDataToFirestore(jumlahInfaq, jumlahKas, jumlahPengeluaran)
            } else {
                Toast.makeText(this, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol kembali
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    // Fungsi untuk membuka DatePicker dialog
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Fungsi untuk validasi input
    private fun validateInput(infaq: String, kas: String, pengeluaran: String): Boolean {
        return tanggalMulai != null && tanggalSelesai != null && infaq.isNotEmpty() && kas.isNotEmpty() && pengeluaran.isNotEmpty()
    }

    // Fungsi untuk menyimpan data ke Firestore
    private fun saveDataToFirestore(infaq: String, kas: String, pengeluaran: String) {
        val laporan = hashMapOf(
            "tanggal_mulai" to tanggalMulai,
            "tanggal_selesai" to tanggalSelesai,
            "jumlah_infaq" to infaq,
            "jumlah_kas" to kas,
            "jumlah_pengeluaran" to pengeluaran,
            "timestamp" to FieldValue.serverTimestamp() // Untuk menambahkan waktu saat data disimpan
        )

        db.collection("laporan_keuangan_masjid")
            .add(laporan)
            .addOnSuccessListener {
                Toast.makeText(this, "Laporan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                // Bersihkan input setelah berhasil menyimpan
                clearInputs()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan laporan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk membersihkan input
    private fun clearInputs() {
        binding.editTextTanggalMulai.text.clear()
        binding.editTextTanggalSelesai.text.clear()
        binding.editTextJumlahInfaq.text.clear()
        binding.editTextJumlahKas.text.clear()
        binding.editTextJumlahPengeluaran.text.clear()
        tanggalMulai = null
        tanggalSelesai = null
    }
}