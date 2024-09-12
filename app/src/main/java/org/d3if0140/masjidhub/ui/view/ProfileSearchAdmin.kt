package org.d3if0140.masjidhub.ui.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.ActivityProfileSearchAdminBinding

class ProfileSearchAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSearchAdminBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSearchAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Get userId from Intent
        val userId = intent.getStringExtra("USER_ID")

        // Logging userId for debugging
        Log.d("ProfileSearchAdmin", "Received userId: $userId")

        if (userId != null && userId.isNotEmpty()) {
            // Load user profile data
            loadUserProfile(userId)
        } else {
            Log.e("ProfileSearchAdmin", "Invalid or missing userId")
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
        }

        // Setup TabLayout for fragments
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showFragment(PostAdminFragment(), userId) // Load PostAdminFragment
                    1 -> showFragment(InfaqAdminFragment(), userId) // Load InfaqAdminFragment
                    2 -> showFragment(JamaahTerdaftarAdminFragment().apply {
                        arguments = Bundle().apply {
                            putString("nama", binding.namaUserDkm.text.toString())
                        }
                    }, null) // Load JamaahTerdaftarSearchFragment without userId
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.backButton.setOnClickListener {
            finish() // Return to previous activity
        }

        binding.infoKas.setOnClickListener {
            val intent = Intent(this, InformasiKasSearchActivity::class.java)
            intent.putExtra("userId", userId) // Ganti dengan nilai userId yang benar
            startActivity(intent)
        }
    }

    private fun loadUserProfile(userId: String) {
        firestore.collection("user").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("nama") ?: "Unknown User"
                    val userAlamat = document.getString("alamat") ?: "Unknown Address"
                    val userImageUrl = document.getString("imageUrl") ?: ""

                    // Update UI with user profile data
                    binding.namaUserDkm.text = userName
                    binding.alamatMasjid.text = userAlamat
                    loadProfileImage(userImageUrl, binding.profileImageDkm)

                    // Set OnClickListener to open image in full screen
                    binding.profileImageDkm.setOnClickListener {
                        val intent = Intent(this, FullScreenImageActivity::class.java)
                        intent.putExtra("IMAGE_URL", userImageUrl)
                        startActivity(intent)
                    }
                } else {
                    Log.e("ProfileSearchAdmin", "User document not found for userId: $userId")
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileSearchAdmin", "Error fetching user document", e)
                Toast.makeText(this, "Error fetching user profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileImage(imageUrl: String, imageView: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.baseline_arrow_back)
            .into(imageView)
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
}
