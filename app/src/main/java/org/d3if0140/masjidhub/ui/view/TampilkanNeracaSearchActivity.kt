package org.d3if0140.masjidhub.ui.view

import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.ActivityTampilkanNeracaSearchBinding
import org.d3if0140.masjidhub.model.LaporanKeuangan
import org.d3if0140.masjidhub.ui.adapter.LaporanAdapter
import java.util.Calendar

class TampilkanNeracaSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTampilkanNeracaSearchBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var laporanAdapter: LaporanAdapter
    private val laporanList = mutableListOf<LaporanKeuangan>()
    private var userId: String? = null
    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTampilkanNeracaSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Ambil userId dari Intent
        userId = intent.getStringExtra("USER_ID")
        Log.d("TampilkanNeracaSearch", "Received userId: $userId")  // Log untuk userId

        laporanAdapter = LaporanAdapter(laporanList)
        binding.recyclerViewNeraca.adapter = laporanAdapter
        binding.recyclerViewNeraca.layoutManager = LinearLayoutManager(this)

        setupSpinners()

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        // Fetch laporan keuangan dari Firestore saat Activity dibuka
        fetchLaporanKeuangan()
    }

    private fun setupSpinners() {
        val months = resources.getStringArray(R.array.bulan_array)
        val years = resources.getStringArray(R.array.tahun_array)

        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)

        binding.spinnerBulan.adapter = monthAdapter
        binding.spinnerTahun.adapter = yearAdapter

        binding.spinnerBulan.setSelection(selectedMonth)
        binding.spinnerTahun.setSelection(years.indexOf(selectedYear.toString()))

        binding.spinnerBulan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedMonth = position
                fetchLaporanKeuanganBulan(selectedMonth, selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerTahun.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedYear = years[position].toInt()
                fetchLaporanKeuanganBulan(selectedMonth, selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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
}
