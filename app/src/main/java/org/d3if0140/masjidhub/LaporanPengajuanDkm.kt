package org.d3if0140.masjidhub

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.VolleyLog.TAG
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityLaporanKasJamaahBinding
import org.d3if0140.masjidhub.databinding.ActivityLaporanPengajuanDkmBinding
import org.d3if0140.masjidhub.databinding.ActivityLaporanPengajuanJamaahBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LaporanPengajuanDkm : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanPengajuanDkmBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var selectedDateCalendar: Calendar
    private var totalPengajuan: Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanPengajuanDkmBinding.inflate(layoutInflater)
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

    companion object {
        private const val TAG = "LaporanPengajuanJamaah"
    }
}
