package org.d3if0140.masjidhub.ui.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.ui.adapter.TransaksiAdapter
import org.d3if0140.masjidhub.ui.adapter.TransaksiBulananAdapter
import org.d3if0140.masjidhub.ui.adapter.TransaksiMingguanAdapter
import org.d3if0140.masjidhub.ui.adapter.TransaksiTahunanAdapter
import org.d3if0140.masjidhub.databinding.ActivityLaporanKeuanganDkmBinding
import org.d3if0140.masjidhub.model.TransaksiBulanan
import org.d3if0140.masjidhub.model.TransaksiHarian
import org.d3if0140.masjidhub.model.TransaksiMingguan
import org.d3if0140.masjidhub.model.TransaksiTahunan
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class LaporanKeuanganDkmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanKeuanganDkmBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var harianAdapter: TransaksiAdapter
    private lateinit var mingguanAdapter: TransaksiMingguanAdapter
    private lateinit var bulananAdapter: TransaksiBulananAdapter
    private lateinit var tahunanAdapter: TransaksiTahunanAdapter

    private var selectedYear: String = "2024"
    private var selectedMonth: String = "Januari"
    private var selectedTipe: String = "Kas" // Default tipe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanKeuanganDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Set layout manager untuk RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Inisialisasi adapter untuk harian dan mingguan
        harianAdapter = TransaksiAdapter(emptyList())
        mingguanAdapter = TransaksiMingguanAdapter(emptyList())
        bulananAdapter = TransaksiBulananAdapter(emptyList())
        tahunanAdapter = TransaksiTahunanAdapter(emptyList())

        // Set adapter default untuk RecyclerView
        binding.recyclerView.adapter = harianAdapter

        // Bottom navigation listener
        binding.bottomNavigationDkm.selectedItemId = R.id.menu_finance
        binding.bottomNavigationDkm.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home_dkm -> true
                R.id.uploadEvent -> {
                    val intent = Intent(this, UnggahActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_finance -> {
                    val intent = Intent(this, LaporanKeuanganDkmActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_profile_dkm -> {
                    val intent = Intent(this, ProfilDkmActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        // Set listener untuk TabLayout
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.tabTextView.text = "Harian"
                        binding.recyclerView.adapter = harianAdapter
                        loadDataHarian()
                    }
                    1 -> {
                        binding.tabTextView.text = "Mingguan"
                        binding.recyclerView.adapter = mingguanAdapter
                        loadDataMingguan()
                    }
                    2 -> {
                        binding.tabTextView.text = "Bulanan"
                        binding.recyclerView.adapter = bulananAdapter
                        loadDataBulanan()
                    }
                    3 -> {
                        binding.tabTextView.text = "Tahunan"
                        binding.recyclerView.adapter = tahunanAdapter
                        loadDataTahunan()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Setup Spinner untuk tahun
        val tahunSpinner = binding.spinnerTahun
        val tahunArray = resources.getStringArray(R.array.tahun_array)
        val tahunAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tahunArray)
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tahunSpinner.adapter = tahunAdapter
        tahunSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedTipe = parent.getItemAtPosition(position).toString()
                updateData()
            }


            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Setup Spinner untuk bulan
        val bulanSpinner = binding.spinnerBulan
        val bulanArray = resources.getStringArray(R.array.bulan_array)
        val bulanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bulanArray)
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bulanSpinner.adapter = bulanAdapter
        bulanSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedMonth = bulanArray[position]
                updateData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Setup Spinner untuk tipe transaksi
        val tipeSpinner = binding.spinnerTipe
        val tipeArray = resources.getStringArray(R.array.tipe_array)
        val tipeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipeArray)
        tipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tipeSpinner.adapter = tipeAdapter
        tipeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTipe = tipeArray[position]
                updateData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        loadDataKeuangan()
    }

    private fun updateData() {
        when (binding.tabLayout.selectedTabPosition) {
            0 -> loadDataHarian()
            1 -> loadDataMingguan()
            2 -> loadDataBulanan()
            3 -> loadDataTahunan()
        }
    }

    private fun loadDataKeuangan() {
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

                val balance = totalIncome - totalExpense

                binding.incomeValue.text = "Rp$totalIncome"
                binding.expenseValue.text = "Rp$totalExpense"
                binding.balanceValue.text = "Rp$balance"

                Log.d(
                    "LaporanKeuangan",
                    "Total Income: Rp$totalIncome, Total Expense: Rp$totalExpense, Balance: Rp$balance"
                )
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("LaporanKeuangan", "Error fetching data: ${exception.message}")
            }
    }

    private fun loadDataHarian() {
        firestore.collection("transaksi_keuangan")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                val transaksiHarian = mutableMapOf<String, Pair<Long, Long>>()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val monthFormat = SimpleDateFormat("MM", Locale.getDefault())
                val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                val monthIndex = resources.getStringArray(R.array.bulan_array).indexOf(selectedMonth) + 1
                val monthString = if (monthIndex < 10) "0$monthIndex" else monthIndex.toString()

                for (document in documents) {
                    val tanggal = document.getString("tanggal") ?: continue
                    val tipe = document.getString("tipe") ?: continue
                    val jumlah = document.getLong("jumlah") ?: 0L

                    try {
                        val date = dateFormat.parse(tanggal) ?: continue
                        val day = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                        val month = monthFormat.format(date)
                        val year = yearFormat.format(date)

                        if (month == monthString && year == selectedYear) {
                            val (income, expense) = transaksiHarian[day] ?: Pair(0L, 0L)

                            val updatedIncome = when {
                                selectedTipe == "Semua" && (tipe == "kas" || tipe == "infaq") -> income + jumlah
                                selectedTipe == "Kas" && tipe == "kas" -> income + jumlah
                                selectedTipe == "Infaq" && tipe == "infaq" -> income + jumlah
                                else -> income
                            }

                            val updatedExpense = when {
                                selectedTipe == "Semua" && tipe == "pengajuan_dana" -> expense + jumlah
                                selectedTipe == "Pengajuan" && tipe == "pengajuan_dana" -> expense + jumlah
                                else -> expense
                            }

                            transaksiHarian[day] = Pair(updatedIncome, updatedExpense)
                        }
                    } catch (e: ParseException) {
                        Log.e("LaporanKeuangan", "Error parsing date: $tanggal", e)
                    }
                }

                val dataHarian = transaksiHarian.map { (tanggal, incomeExpense) ->
                    TransaksiHarian(tanggal, incomeExpense.first, incomeExpense.second)
                }

                if (dataHarian.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.tabTextView.visibility = View.GONE
                    harianAdapter.updateData(dataHarian)
                } else {
                    binding.recyclerView.visibility = View.GONE
                    binding.tabTextView.visibility = View.VISIBLE
                    binding.tabTextView.text = "Tidak ada data harian"
                }

                Log.d("LaporanKeuangan", "Data Harian: $dataHarian")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("LaporanKeuangan", "Error fetching data harian: ${exception.message}")
            }
    }


    private fun loadDataMingguan() {
        firestore.collection("transaksi_keuangan")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                val transaksiMingguan = mutableMapOf<String, Pair<Long, Long>>()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val monthFormat = SimpleDateFormat("MM", Locale.getDefault())
                val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                val monthIndex = resources.getStringArray(R.array.bulan_array).indexOf(selectedMonth) + 1
                val monthString = if (monthIndex < 10) "0$monthIndex" else monthIndex.toString()

                for (document in documents) {
                    val tanggal = document.getString("tanggal") ?: continue
                    val tipe = document.getString("tipe") ?: continue
                    val jumlah = document.getLong("jumlah") ?: 0L

                    try {
                        val date = dateFormat.parse(tanggal) ?: continue
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        val month = monthFormat.format(date)
                        val year = yearFormat.format(date)

                        // Pastikan hanya data dari bulan dan tahun yang dipilih
                        if (month == monthString && year == selectedYear) {
                            // Hitung minggu dari awal bulan yang dipilih
                            calendar.firstDayOfWeek = Calendar.MONDAY
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            val firstDayOfMonth = calendar.time
                            val currentWeekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)

                            val weekKey = "Minggu $currentWeekOfMonth"
                            val (income, expense) = transaksiMingguan[weekKey] ?: Pair(0L, 0L)

                            val updatedIncome = when {
                                selectedTipe == "Semua" && (tipe == "kas" || tipe == "infaq") -> income + jumlah
                                selectedTipe == "Kas" && tipe == "kas" -> income + jumlah
                                selectedTipe == "Infaq" && tipe == "infaq" -> income + jumlah
                                else -> income
                            }

                            val updatedExpense = when {
                                selectedTipe == "Semua" && tipe == "pengajuan_dana" -> expense + jumlah
                                selectedTipe == "Pengajuan" && tipe == "pengajuan_dana" -> expense + jumlah
                                else -> expense
                            }

                            transaksiMingguan[weekKey] = Pair(updatedIncome, updatedExpense)
                        }
                    } catch (e: ParseException) {
                        Log.e("LaporanKeuangan", "Error parsing date: $tanggal", e)
                    }
                }

                val dataMingguan = transaksiMingguan.map { (week, incomeExpense) ->
                    TransaksiMingguan(week, incomeExpense.first, incomeExpense.second)
                }

                if (dataMingguan.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.tabTextView.visibility = View.GONE
                    mingguanAdapter.updateData(dataMingguan)
                } else {
                    binding.recyclerView.visibility = View.GONE
                    binding.tabTextView.visibility = View.VISIBLE
                    binding.tabTextView.text = "Tidak ada data mingguan"
                }

                Log.d("LaporanKeuangan", "Data Mingguan: $dataMingguan")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("LaporanKeuangan", "Error fetching data mingguan: ${exception.message}")
            }
    }




    private fun loadDataBulanan() {
        firestore.collection("transaksi_keuangan")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                val transaksiBulanan = mutableMapOf<String, Pair<Long, Long>>()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val monthFormat = SimpleDateFormat("MM", Locale.getDefault())
                val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

                for (document in documents) {
                    val tanggal = document.getString("tanggal") ?: continue
                    val tipe = document.getString("tipe") ?: continue
                    val jumlah = document.getLong("jumlah") ?: 0L

                    try {
                        val date = dateFormat.parse(tanggal) ?: continue
                        val month = monthFormat.format(date)
                        val year = yearFormat.format(date)

                        if (year == selectedYear) {
                            val monthKey = "Bulan $month"
                            val (income, expense) = transaksiBulanan[monthKey] ?: Pair(0L, 0L)

                            val updatedIncome = when {
                                selectedTipe == "Semua" && (tipe == "kas" || tipe == "infaq") -> income + jumlah
                                selectedTipe == "Kas" && tipe == "kas" -> income + jumlah
                                selectedTipe == "Infaq" && tipe == "infaq" -> income + jumlah
                                else -> income
                            }

                            val updatedExpense = when {
                                selectedTipe == "Semua" && tipe == "pengajuan_dana" -> expense + jumlah
                                selectedTipe == "Pengajuan" && tipe == "pengajuan_dana" -> expense + jumlah
                                else -> expense
                            }

                            transaksiBulanan[monthKey] = Pair(updatedIncome, updatedExpense)
                        }
                    } catch (e: ParseException) {
                        Log.e("LaporanKeuangan", "Error parsing date: $tanggal", e)
                    }
                }

                val dataBulanan = transaksiBulanan.map { (month, incomeExpense) ->
                    TransaksiBulanan(month, incomeExpense.first, incomeExpense.second)
                }

                if (dataBulanan.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.tabTextView.visibility = View.GONE
                    bulananAdapter.updateData(dataBulanan)
                } else {
                    binding.recyclerView.visibility = View.GONE
                    binding.tabTextView.visibility = View.VISIBLE
                    binding.tabTextView.text = "Tidak ada data bulanan"
                }

                Log.d("LaporanKeuangan", "Data Bulanan: $dataBulanan")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("LaporanKeuangan", "Error fetching data bulanan: ${exception.message}")
            }
    }


    private fun loadDataTahunan() {
        firestore.collection("transaksi_keuangan")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                val transaksiTahunan = mutableMapOf<String, Pair<Long, Long>>()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

                for (document in documents) {
                    val tanggal = document.getString("tanggal") ?: continue
                    val tipe = document.getString("tipe") ?: continue
                    val jumlah = document.getLong("jumlah") ?: 0L

                    try {
                        val date = dateFormat.parse(tanggal) ?: continue
                        val year = yearFormat.format(date)

                        if (year == selectedYear) {
                            val yearKey = "Tahun $year"
                            val (income, expense) = transaksiTahunan[yearKey] ?: Pair(0L, 0L)

                            val updatedIncome = when {
                                selectedTipe == "Semua" && (tipe == "kas" || tipe == "infaq") -> income + jumlah
                                selectedTipe == "Kas" && tipe == "kas" -> income + jumlah
                                selectedTipe == "Infaq" && tipe == "infaq" -> income + jumlah
                                else -> income
                            }

                            val updatedExpense = when {
                                selectedTipe == "Semua" && tipe == "pengajuan_dana" -> expense + jumlah
                                selectedTipe == "Pengajuan" && tipe == "pengajuan_dana" -> expense + jumlah
                                else -> expense
                            }

                            transaksiTahunan[yearKey] = Pair(updatedIncome, updatedExpense)
                        }
                    } catch (e: ParseException) {
                        Log.e("LaporanKeuangan", "Error parsing date: $tanggal", e)
                    }
                }

                val dataTahunan = transaksiTahunan.map { (year, incomeExpense) ->
                    TransaksiTahunan(year, incomeExpense.first, incomeExpense.second)
                }

                if (dataTahunan.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.tabTextView.visibility = View.GONE
                    tahunanAdapter.updateData(dataTahunan)
                } else {
                    binding.recyclerView.visibility = View.GONE
                    binding.tabTextView.visibility = View.VISIBLE
                    binding.tabTextView.text = "Tidak ada data tahunan"
                }

                Log.d("LaporanKeuangan", "Data Tahunan: $dataTahunan")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("LaporanKeuangan", "Error fetching data tahunan: ${exception.message}")
            }
    }


}
