package org.d3if0140.masjidhub.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.ui.adapter.MasjidAdminAdapter
import org.d3if0140.masjidhub.databinding.ActivityAdminListMasjidBinding
import org.d3if0140.masjidhub.model.Masjid

class AdminListMasjid : AppCompatActivity() {
    private lateinit var binding: ActivityAdminListMasjidBinding
    private lateinit var masjidAdminAdapter: MasjidAdminAdapter
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
        masjidAdminAdapter = MasjidAdminAdapter(masjidList) { masjid ->
            val intent = Intent(this, ProfileSearchAdmin::class.java).apply {
                putExtra("USER_ID", masjid.userId)
                putExtra("USER_NAME", masjid.nama)
                putExtra("USER_ALAMAT", masjid.alamat)
                putExtra("USER_IMAGE_URL", masjid.imageUrl)
            }
            startActivity(intent)
        }
        binding.recyclerViewMasjid.adapter = masjidAdminAdapter

        fetchMasjidData()
    }

    private fun fetchMasjidData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("user")
            .whereEqualTo("role", "pengurus_dkm")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userId = document.id
                    val name = document.getString("nama") ?: ""
                    val address = document.getString("alamat") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    masjidList.add(Masjid(userId, name, address, imageUrl))
                }
                masjidAdminAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }
}
