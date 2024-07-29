package org.d3if0140.masjidhub

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.VolleyLog.TAG
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityLaporanInfaqBinding
import org.d3if0140.masjidhub.databinding.ActivityLaporanInfaqJamaahBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LaporanInfaqJamaah : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanInfaqJamaahBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var selectedDateCalendar: Calendar
    private var totalInfaq: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanInfaqJamaahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        selectedDateCalendar = Calendar.getInstance()

        binding.chooseDateButton.setOnClickListener {
            showDatePickerDialog()
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

    companion object {
        private const val TAG = "LaporanInfaqJamaah"
    }
}