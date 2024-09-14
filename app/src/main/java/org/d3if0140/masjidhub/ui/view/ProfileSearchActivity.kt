package org.d3if0140.masjidhub.ui.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.ActivityProfileSearchBinding

class ProfileSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSearchBinding
    private lateinit var firestore: FirebaseFirestore
    private var userId: String? = null
//    private var longitude: Double = 0.0
//    private var latitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Get data from Intent
        userId = intent.getStringExtra("USER_ID")
        val userName = intent.getStringExtra("USER_NAME")
        val userAlamat = intent.getStringExtra("USER_ALAMAT")
        val userImageUrl = intent.getStringExtra("USER_IMAGE_URL")
        val latitude = intent.getDoubleExtra("USER_LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("USER_LONGITUDE", 0.0)

        // Log untuk memastikan koordinat sudah benar
        Log.d("ProfileSearchActivity", "Latitude: $latitude, Longitude: $longitude")

        // Logging userId for debugging
        Log.d("ProfileSearchActivity", "Received userId: $userId")

        binding.namaUserDkm.text = userName
        binding.alamatMasjid.text = userAlamat

        // Load profile image
        if (userImageUrl != null && userImageUrl.isNotEmpty()) {
            loadProfileImage(userImageUrl, binding.profileImageDkm)
        } else {
            binding.profileImageDkm.setImageResource(R.drawable.baseline_person_black)
        }

        // Setup TabLayout
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showFragment(PostSearchFragment(), userId)
                    1 -> showFragment(InfaqSearchFragment(), userId)
                    2 -> showFragment(JamaahTerdaftarSerachFragment().apply {
                        arguments = Bundle().apply {
                            putString("nama", binding.namaUserDkm.text.toString())
                        }
                    }, null) // No userId needed here
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, CariMasjidActivity::class.java))
        }

        binding.infoKas.setOnClickListener {
            val intent = Intent(this, InformasiKasSearchActivity::class.java)
            intent.putExtra("userId", userId) // Ganti dengan nilai userId yang benar
            startActivity(intent)
        }

        // Tombol untuk membuka Google Maps dengan koordinat
        binding.buttonDirectMaps.setOnClickListener {
            openGoogleMaps(latitude, longitude)
        }

    }

    private fun showFragment(fragment: Fragment, userId: String?) {
        userId?.let {
            val bundle = Bundle().apply { putString("USER_ID", it) }
            fragment.arguments = bundle
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun loadProfileImage(imageUrl: String, imageView: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.baseline_arrow_back)
            .into(imageView)
    }
    private fun openGoogleMaps(latitude: Double, longitude: Double) {
        if (latitude != 0.0 && longitude != 0.0) {
            // Buat URI Google Maps dengan format 'geo:latitude,longitude'
            val gmmIntentUri = Uri.parse("geo:$longitude,$latitude?q=$longitude,$latitude")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

                startActivity(mapIntent)
        }

    }
}

