package org.d3if0140.masjidhub

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityLaporanKasBinding
import java.text.SimpleDateFormat
import java.util.*

class LaporanKasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanKasBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var selectedDateCalendar: Calendar
    private var totalKas: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanKasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        selectedDateCalendar = Calendar.getInstance()

        binding.chooseDateButton.setOnClickListener {
            showDatePickerDialog()
        }

        binding.perbaruiButton.setOnClickListener {
            saveTotalInSharedPreferences()
            sendTotalToAdminKeuangan()
        }
    }

    private fun showDatePickerDialog() {
        val year = selectedDateCalendar.get(Calendar.YEAR)
        val month = selectedDateCalendar.get(Calendar.MONTH)
        val day = selectedDateCalendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                selectedDateCalendar.set(year, monthOfYear, dayOfMonth)
                binding.selectedDateTextView.text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(selectedDateCalendar.time)
                showDataForSelectedDate()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showDataForSelectedDate() {
        val selectedDate = selectedDateCalendar.time
        val firestoreDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = firestoreDateFormat.format(selectedDate)

        firestore.collection("kas_mingguan")
            .whereEqualTo("tanggal", formattedDate)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dataList = mutableListOf<DataKas>()
                totalKas = 0.0

                for (document in querySnapshot.documents) {
                    val email = document.getString("email")
                    val jumlah = document.getDouble("jumlah")

                    if (email != null && jumlah != null) {
                        val data = DataKas(email, jumlah)
                        dataList.add(data)
                        totalKas += jumlah
                    }
                }

                updateUIWithData(dataList)
                binding.textTotalKas.text = "Total Kas: $totalKas"
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting data for date $formattedDate", exception)
            }
    }

    private fun updateUIWithData(dataList: List<DataKas>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = DataKasAdapter(dataList)
        binding.recyclerView.adapter = adapter
    }

    private fun saveTotalInSharedPreferences() {
        val sharedPreferences = getSharedPreferences("AdminKeuanganPrefs", MODE_PRIVATE)
        val lastUpdatedDate = sharedPreferences.getString("lastUpdatedKasDate", "")

        val selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDateCalendar.time)
        if (lastUpdatedDate != selectedDate) {
            val intent = Intent(this, AdminKeuangan::class.java)
            intent.putExtra("TOTAL_KAS", totalKas)
            startActivity(intent)

            // Simpan tanggal terakhir yang diperbarui
            val editor = sharedPreferences.edit()
            editor.putString("lastUpdatedKasDate", selectedDate)
            editor.apply()
        } else {
            // Tampilkan pesan bahwa data untuk tanggal ini sudah diperbarui sebelumnya
            Log.d(TAG, "Data dari tanggal yang dipilih sudah di perbarui.")
        }
    }

    private fun sendTotalToAdminKeuangan() {
        val intent = Intent(this, AdminKeuangan::class.java).apply {
            putExtra("TOTAL_KAS", totalKas)
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "LaporanKasActivity"
    }
}
