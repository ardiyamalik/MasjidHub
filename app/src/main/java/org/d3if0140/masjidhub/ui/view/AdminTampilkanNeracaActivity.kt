package org.d3if0140.masjidhub.ui.view

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.databinding.ActivityAdminTampilkanNeracaBinding
import org.d3if0140.masjidhub.model.LaporanKeuangan
import org.d3if0140.masjidhub.ui.adapter.LaporanAdapter
import java.util.Calendar

class AdminTampilkanNeracaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminTampilkanNeracaBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var laporanAdapter: LaporanAdapter
    private val laporanList = mutableListOf<LaporanKeuangan>()
    private var userId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminTampilkanNeracaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Ambil userId dari Intent
        userId = intent.getStringExtra("USER_ID")
        Log.d("TampilkanNeracaSearch", "Received userId: $userId")  // Log untuk userId

        laporanAdapter = LaporanAdapter(laporanList)
        binding.recyclerViewNeraca.adapter = laporanAdapter
        binding.recyclerViewNeraca.layoutManager = LinearLayoutManager(this)

        // Menampilkan dialog filter tanggal/bulan
        binding.editTextFilterBulan.setOnClickListener {
            showMonthPicker { month, year ->
                fetchLaporanKeuanganBulan(month, year)
            }
        }

        // Fetch laporan keuangan dari Firestore saat Activity dibuka
        fetchLaporanKeuangan()
    }

    private fun fetchLaporanKeuangan() {
        if (userId == null) {
            Log.e("TampilkanNeracaSearch", "userId is null, cannot fetch data.")
            return
        }

        // Masukkan query filter untuk masjidId
        Log.d("TampilkanNeracaSearch", "Fetching laporan keuangan for userId: $userId")  // Log query Firestore
        db.collection("laporan_keuangan_masjid")
            .whereEqualTo("userId", userId) // Filter berdasarkan userId
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Log.d("TampilkanNeracaSearch", "No laporan keuangan found for userId: $userId")  // Log jika tidak ada data
                } else {
                    Log.d("TampilkanNeracaSearch", "Found ${snapshot.size()} laporan keuangan for userId: $userId")  // Log jumlah data
                }

                laporanList.clear()
                for (document in snapshot.documents) {
                    val laporan = document.toObject(LaporanKeuangan::class.java)
                    if (laporan != null) {
                        Log.d("TampilkanNeracaSearch", "Laporan: $laporan")  // Log setiap item laporan
                        laporanList.add(laporan)
                    }
                }
                laporanAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("TampilkanNeracaSearch", "Error fetching data: ${e.message}")  // Log error saat mengambil data
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchLaporanKeuanganBulan(month: Int, year: Int) {
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }.time

        val endOfMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }.time

        Log.d("TampilkanNeracaSearch", "Fetching laporan keuangan for userId: $userId, Month: $month, Year: $year")  // Log query Firestore berdasarkan bulan

        db.collection("laporan_keuangan_masjid")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", startOfMonth)
            .whereLessThanOrEqualTo("timestamp", endOfMonth)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Log.d("TampilkanNeracaSearch", "No laporan keuangan found for userId: $userId in month $month, year $year")  // Log jika tidak ada data
                } else {
                    Log.d("TampilkanNeracaSearch", "Found ${snapshot.size()} laporan keuangan for userId: $userId in month $month, year $year")  // Log jumlah data
                }

                laporanList.clear()
                for (document in snapshot.documents) {
                    val laporan = document.toObject(LaporanKeuangan::class.java)
                    if (laporan != null) {
                        Log.d("TampilkanNeracaSearch", "Laporan: $laporan")  // Log setiap item laporan
                        laporanList.add(laporan)
                    }
                }
                laporanAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("TampilkanNeracaSearch", "Error fetching data: ${e.message}")  // Log error saat mengambil data
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun showMonthPicker(onMonthSelected: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, _ ->
            Log.d("TampilkanNeracaSearch", "Month picker selected: $selectedMonth, Year: $selectedYear")  // Log bulan dan tahun yang dipilih dari DatePicker
            onMonthSelected(selectedMonth, selectedYear)
        }, year, month, calendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.show()
    }
}