package org.d3if0140.masjidhub

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityLaporanKeuanganBinding

class LaporanKeuanganActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanKeuanganBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanKeuanganBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set listener untuk TabLayout
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Update TextView berdasarkan tab yang dipilih
                when (tab?.position) {
                    0 -> binding.tabTextView.text = "Harian"
                    1 -> binding.tabTextView.text = "Mingguan"
                    2 -> binding.tabTextView.text = "Bulanan"
                    3 -> binding.tabTextView.text = "Tahunan"
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Fungsi saat menekan month_selector
        binding.monthSelector.setOnClickListener {
            showDatePicker()
        }

        // Fungsi untuk tombol filter
        binding.actionFilter.setOnClickListener {
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
        }

        // Panggil fungsi untuk load data keuangan
        loadDataKeuangan()
    }

    private fun loadDataKeuangan() {
        // Query Firestore untuk mendapatkan data infaq dan kas yang statusnya approved
        firestore.collection("transaksi_keuangan")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                var totalIncome = 0L
                var totalExpense = 0L

                for (document in documents) {
                    val tipe = document.getString("tipe")
                    val jumlah = document.getLong("jumlah") ?: 0L

                    when (tipe) {
                        "infaq", "kas" -> totalIncome += jumlah
                        "pengajuan_dana" -> totalExpense += jumlah
                    }
                }

                // Update UI dengan hasil perhitungan
                val balance = totalIncome - totalExpense

                binding.incomeValue.text = "Rp$totalIncome"
                binding.expenseValue.text = "Rp$totalExpense"
                binding.balanceValue.text = "Rp$balance"
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
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
