package org.d3if0140.masjidhub.ui.view

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.databinding.ActivityTampilkanNeracaBinding
import org.d3if0140.masjidhub.model.LaporanKeuangan
import org.d3if0140.masjidhub.ui.adapter.LaporanAdapter
import java.util.Calendar

class TampilkanNeracaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTampilkanNeracaBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var laporanAdapter: LaporanAdapter
    private val laporanList = mutableListOf<LaporanKeuangan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTampilkanNeracaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

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
        db.collection("laporan_keuangan_masjid")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                laporanList.clear()
                for (document in snapshot.documents) {
                    val laporan = document.toObject(LaporanKeuangan::class.java)
                    laporan?.let { laporanList.add(it) }
                }
                laporanAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchLaporanKeuanganBulan(month: Int, year: Int) {
        // Mengambil laporan keuangan berdasarkan bulan dan tahun tertentu
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

        db.collection("laporan_keuangan_masjid")
            .whereGreaterThanOrEqualTo("timestamp", startOfMonth)
            .whereLessThanOrEqualTo("timestamp", endOfMonth)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                laporanList.clear()
                for (document in snapshot.documents) {
                    val laporan = document.toObject(LaporanKeuangan::class.java)
                    laporan?.let { laporanList.add(it) }
                }
                laporanAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showMonthPicker(onMonthSelected: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, _ ->
            onMonthSelected(selectedMonth, selectedYear)
        }, year, month, calendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.show()
    }
}

