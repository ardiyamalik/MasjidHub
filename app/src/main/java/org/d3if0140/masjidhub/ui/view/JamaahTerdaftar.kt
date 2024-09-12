package org.d3if0140.masjidhub.ui.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.ui.adapter.JamaahAdapter
import org.d3if0140.masjidhub.databinding.ActivityJamaahTerdaftarBinding
import org.d3if0140.masjidhub.model.Jamaah

class JamaahTerdaftar : AppCompatActivity() {

    private lateinit var binding: ActivityJamaahTerdaftarBinding
    private val jamaahList = mutableListOf<Jamaah>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJamaahTerdaftarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewJamaah.layoutManager = LinearLayoutManager(this)

        binding.backButton.setOnClickListener {
            val intent = Intent(this, ProfilDkmActivity::class.java)
            startActivity(intent)
        }

        val masjidName = intent.getStringExtra("nama")?.trim()
        Log.d("JamaahTerdaftarActivity", "Masjid Name: $masjidName")
        masjidName?.let {
            fetchJamaahList(it)
        } ?: Log.d("JamaahTerdaftarActivity", "Masjid name is null or empty")
    }

    private fun fetchJamaahList(currentMasjidName: String) {
        Log.d("JamaahTerdaftarActivity", "Fetching Jamaah list for Masjid: $currentMasjidName")
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("user")
            .whereEqualTo("role", "jamaah")
            .whereEqualTo("dkm", currentMasjidName)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("JamaahTerdaftarActivity", "Successfully fetched documents")
                jamaahList.clear()
                for (document in documents) {
                    val jamaah = document.toObject(Jamaah::class.java)
                    jamaahList.add(jamaah)
                    Log.d("JamaahTerdaftarActivity", "Added Jamaah: $jamaah")
                }
                setupAdapter()
                Log.d("JamaahTerdaftarActivity", "Jamaah list size: ${jamaahList.size}")

                if (jamaahList.isEmpty()) {
                    Log.d("JamaahTerdaftarActivity", "Jamaah list is empty")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("JamaahTerdaftarActivity", "Error getting documents: ", exception)
            }
    }

    private fun setupAdapter() {
        val adapter = JamaahAdapter(jamaahList) { jamaah ->
            // Handle item click here
            Log.d("JamaahTerdaftarActivity", "Clicked on: ${jamaah.nama}")
        }
        binding.recyclerViewJamaah.adapter = adapter
    }
}
