package org.d3if0140.masjidhub

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityLaporanKeuanganBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LaporanKeuanganActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanKeuanganBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var harianAdapter: TransaksiAdapter
    private lateinit var mingguanAdapter: TransaksiMingguanAdapter
    private lateinit var bulananAdapter: TransaksiBulananAdapter
    private lateinit var tahunanAdapter: TransaksiTahunanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanKeuanganBinding.inflate(layoutInflater)
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

        binding.actionFilter.setOnClickListener {
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
        }

        loadDataKeuangan()
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

                for (document in documents) {
                    val tanggal = document.getString("tanggal") ?: continue
                    val tipe = document.getString("tipe") ?: continue
                    val jumlah = document.getLong("jumlah") ?: 0L

                    val (income, expense) = transaksiHarian[tanggal] ?: Pair(0L, 0L)
                    val updatedIncome =
                        if (tipe == "infaq" || tipe == "kas") income + jumlah else income
                    val updatedExpense = if (tipe == "pengajuan_dana") expense + jumlah else expense

                    transaksiHarian[tanggal] = Pair(updatedIncome, updatedExpense)
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
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        firestore.collection("transaksi_keuangan")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                val transaksiMingguan = mutableMapOf<String, Pair<Long, Long>>()
                val dateFormat = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ) // Sesuaikan dengan format tanggal di Firestore

                for (document in documents) {
                    val tanggal = document.getString("tanggal") ?: continue
                    val tipe = document.getString("tipe") ?: continue
                    val jumlah = document.getLong("jumlah") ?: 0L

                    val date = try {
                        dateFormat.parse(tanggal)
                    } catch (e: ParseException) {
                        Log.e("LaporanKeuangan", "Error parsing date: $tanggal", e)
                        continue
                    }

                    val weekOfMonth = Calendar.getInstance().apply {
                        time = date
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.DAY_OF_MONTH, get(Calendar.DAY_OF_MONTH))
                    }.get(Calendar.WEEK_OF_MONTH)

                    val mingguKey = "Minggu $weekOfMonth"

                    val (income, expense) = transaksiMingguan[mingguKey] ?: Pair(0L, 0L)
                    val updatedIncome =
                        if (tipe == "infaq" || tipe == "kas") income + jumlah else income
                    val updatedExpense = if (tipe == "pengajuan_dana") expense + jumlah else expense

                    transaksiMingguan[mingguKey] = Pair(updatedIncome, updatedExpense)
                }

                val dataMingguan = transaksiMingguan.map { (minggu, incomeExpense) ->
                    TransaksiMingguan(minggu, incomeExpense.first, incomeExpense.second)
                }

                mingguanAdapter.updateData(dataMingguan)
                Log.d("LaporanKeuangan", "Data Mingguan: $dataMingguan")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("LaporanKeuangan", "Error fetching data mingguan: ${exception.message}")
            }
    }


    private fun loadDataBulanan() {
        val dateFormat =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Ubah format sesuai penyimpanan
        val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        firestore.collection("transaksi_keuangan")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                val transaksiBulanan = mutableMapOf<String, Pair<Long, Long>>()

                for (document in documents) {
                    val tanggal = document.getString("tanggal") ?: continue
                    val tipe = document.getString("tipe") ?: continue
                    val jumlah = document.getLong("jumlah") ?: 0L

                    try {
                        val date = dateFormat.parse(tanggal) ?: continue
                        val monthKey = monthFormat.format(date)

                        val (income, expense) = transaksiBulanan[monthKey] ?: Pair(0L, 0L)
                        val updatedIncome =
                            if (tipe == "infaq" || tipe == "kas") income + jumlah else income
                        val updatedExpense =
                            if (tipe == "pengajuan_dana") expense + jumlah else expense

                        transaksiBulanan[monthKey] = Pair(updatedIncome, updatedExpense)
                    } catch (e: ParseException) {
                        Log.e("LaporanKeuangan", "Error parsing date: $tanggal", e)
                    }
                }

                val dataBulanan = transaksiBulanan.map { (bulan, incomeExpense) ->
                    TransaksiBulanan(bulan, incomeExpense.first, incomeExpense.second)
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
        val dateFormat =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Ubah format sesuai penyimpanan
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        firestore.collection("transaksi_keuangan")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                val transaksiTahunan = mutableMapOf<String, Pair<Long, Long>>()

                for (document in documents) {
                    val tanggal = document.getString("tanggal") ?: continue
                    val tipe = document.getString("tipe") ?: continue
                    val jumlah = document.getLong("jumlah") ?: 0L

                    try {
                        val date = dateFormat.parse(tanggal) ?: continue
                        val yearKey = yearFormat.format(date)

                        val (income, expense) = transaksiTahunan[yearKey] ?: Pair(0L, 0L)
                        val updatedIncome =
                            if (tipe == "infaq" || tipe == "kas") income + jumlah else income
                        val updatedExpense =
                            if (tipe == "pengajuan_dana") expense + jumlah else expense

                        transaksiTahunan[yearKey] = Pair(updatedIncome, updatedExpense)
                    } catch (e: ParseException) {
                        Log.e("LaporanKeuangan", "Error parsing date: $tanggal", e)
                    }
                }

                val dataTahunan = transaksiTahunan.map { (tahun, incomeExpense) ->
                    TransaksiTahunan(tahun, incomeExpense.first, incomeExpense.second)
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
