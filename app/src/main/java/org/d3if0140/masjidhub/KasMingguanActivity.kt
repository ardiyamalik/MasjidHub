package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.d3if0140.masjidhub.databinding.ActivityKasMingguanBinding
import org.d3if0140.masjidhub.model.KasMingguan

class KasMingguanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKasMingguanBinding
    private lateinit var adapter: KasMingguanAdapter
    private val kasMingguanList = mutableListOf<KasMingguan>()
    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKasMingguanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = KasMingguanAdapter(kasMingguanList, this)
        binding.recyclerView.adapter = adapter

        loadKasMingguan()

        binding.backButton.setOnClickListener{
            startActivity(Intent(this, AdminDashboard::class.java))
        }
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
