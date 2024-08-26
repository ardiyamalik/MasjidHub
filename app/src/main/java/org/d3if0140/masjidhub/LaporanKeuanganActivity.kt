package org.d3if0140.masjidhub

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.datepicker.MaterialDatePicker
import org.d3if0140.masjidhub.databinding.ActivityLaporanKeuanganBinding

class LaporanKeuanganActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanKeuanganBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanKeuanganBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Fungsi saat menekan month_selector
        binding.monthSelector.setOnClickListener {
            showDatePicker()
        }

        // Fungsi untuk tombol filter
        binding.actionFilter.setOnClickListener {
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
        }

    }

    // Fungsi untuk menampilkan Date Picker
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Format dan set tanggal ke TextView
                val selectedDate = "$dayOfMonth-${month + 1}-$year"
                binding.monthSelector.text = selectedDate
            },
            2024, 7, 27
        )
        datePickerDialog.setTitle("Pilih Tanggal")
        datePickerDialog.show()
    }

}