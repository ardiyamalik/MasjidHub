package org.d3if0140.masjidhub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.d3if0140.masjidhub.model.KasMingguan

class KasMingguanActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: KasMingguanAdapter
    private val kasMingguanList = mutableListOf<KasMingguan>()
    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kas_mingguan)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = KasMingguanAdapter(kasMingguanList, this) // Pass context here
        recyclerView.adapter = adapter

        loadKasMingguan()
    }

    private fun loadKasMingguan() {
        listenerRegistration = db.collection("kas_mingguan")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                kasMingguanList.clear()
                snapshots?.forEach { document ->
                    val kasMingguan = document.toObject(KasMingguan::class.java).apply {
                        id = document.id
                    }
                    kasMingguanList.add(kasMingguan)
                }
                adapter.notifyDataSetChanged()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }
}
