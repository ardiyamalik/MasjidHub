package org.d3if0140.masjidhub

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityLaporanInfaqBinding
import java.text.SimpleDateFormat
import java.util.*

class LaporanInfaqActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanInfaqBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var selectedDateCalendar: Calendar
    private var totalInfaq: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanInfaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        selectedDateCalendar = Calendar.getInstance()

        binding.backButton.setOnClickListener{
            intent = Intent(this, AdminKeuangan::class.java)
            startActivity(intent)
        }

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

        firestore.collection("infaq_masjid")
            .whereEqualTo("tanggal", formattedDate)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dataList = mutableListOf<DataInfaq>()
                totalInfaq = 0.0

                for (document in querySnapshot.documents) {
                    val userEmail = document.getString("userEmail")
                    val jumlahInfaq = document.getDouble("jumlahInfaq")

                    if (userEmail != null && jumlahInfaq != null) {
                        val data = DataInfaq(userEmail, jumlahInfaq)
                        dataList.add(data)
                        totalInfaq += jumlahInfaq
                    }
                }

                updateUIWithData(dataList)
                binding.textTotalInfaq.text = "Total Infaq: $totalInfaq"
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting data for date $formattedDate", exception)
            }
    }

    private fun updateUIWithData(dataList: List<DataInfaq>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = DataInfaqAdapter(dataList)
        binding.recyclerView.adapter = adapter
    }

    private fun saveTotalInSharedPreferences() {
        val sharedPreferences = getSharedPreferences("AdminKeuanganPrefs", MODE_PRIVATE)
        val lastUpdatedDate = sharedPreferences.getString("lastUpdatedInfaqDate", "")

        val selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDateCalendar.time)
        if (lastUpdatedDate != selectedDate) {
            val intent = Intent(this, AdminKeuangan::class.java)
            intent.putExtra("TOTAL_INFAQ", totalInfaq)
            startActivity(intent)

            // Simpan tanggal terakhir yang diperbarui
            val editor = sharedPreferences.edit()
            editor.putString("lastUpdatedInfaqDate", selectedDate)
            editor.apply()
        } else {
            // Tampilkan pesan bahwa data untuk tanggal ini sudah diperbarui sebelumnya
            Log.d(TAG, "Data dari tanggal yang dipilih sudah di perbarui.")
        }
    }

    private fun sendTotalToAdminKeuangan() {
        val intent = Intent(this, AdminKeuangan::class.java).apply {
            putExtra("TOTAL_INFAQ", totalInfaq)
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "LaporanInfaqActivity"
    }
}
