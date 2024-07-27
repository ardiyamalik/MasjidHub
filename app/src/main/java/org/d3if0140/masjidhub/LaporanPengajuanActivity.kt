package org.d3if0140.masjidhub

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityLaporanPengajuanBinding
import java.text.SimpleDateFormat
import java.util.*

class LaporanPengajuanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanPengajuanBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var selectedDateCalendar: Calendar
    private var totalPengajuan: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanPengajuanBinding.inflate(layoutInflater)
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
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                selectedDateCalendar.set(year, monthOfYear, dayOfMonth)
                binding.selectedDateTextView.text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(selectedDateCalendar.time)

                // Setelah memilih tanggal, langsung panggil fungsi untuk menampilkan data
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

        firestore.collection("pengajuan_dana")
            .whereEqualTo("tanggal", formattedDate)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dataList = mutableListOf<DataPengajuan>()
                totalPengajuan = 0.0

                for (document in querySnapshot.documents) {
                    val userEmail = document.getString("userEmail")
                    val jumlah = document.getDouble("jumlah")

                    if (userEmail != null && jumlah != null) {
                        val data = DataPengajuan(userEmail, jumlah)
                        dataList.add(data)
                        totalPengajuan += jumlah
                    }
                }

                updateUIWithData(dataList)
                binding.textTotalPengajuan.text = "Total Pengajuan: $totalPengajuan"
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting data for date $formattedDate", exception)
            }
    }

    private fun updateUIWithData(dataList: List<DataPengajuan>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = DataPengajuanAdapter(dataList)
        binding.recyclerView.adapter = adapter
    }

    private fun saveTotalInSharedPreferences() {
        val sharedPreferences = getSharedPreferences("AdminKeuanganPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("totalPengajuan", totalPengajuan.toFloat())
        editor.apply()
    }

    private fun sendTotalToAdminKeuangan() {
        val intent = Intent(this, AdminKeuangan::class.java).apply {
            putExtra("TOTAL_PENGAJUAN", totalPengajuan)
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "LaporanPengajuanActivity"
    }
}
