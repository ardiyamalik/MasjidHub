package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityAdminListMasjidBinding

class AdminListMasjid : AppCompatActivity() {
    private lateinit var binding: ActivityAdminListMasjidBinding
    private lateinit var masjidAdapter: MasjidAdapter
    private val masjidList = mutableListOf<Masjid>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminListMasjidBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            val intent = Intent(this, AdminDashboard::class.java)
            startActivity(intent)
        }

        binding.recyclerViewMasjid.layoutManager = LinearLayoutManager(this)
        masjidAdapter = MasjidAdapter(masjidList)
        binding.recyclerViewMasjid.adapter = masjidAdapter

        fetchMasjidData()
    }

    private fun fetchMasjidData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("user")
            .whereEqualTo("role", "pengurus_dkm")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val nama = document.getString("nama") ?: ""
                    val alamat = document.getString("alamat") ?: ""
                    masjidList.add(Masjid(nama, alamat))
                }
                masjidAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }
}
