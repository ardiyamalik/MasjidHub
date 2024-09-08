package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityProfileSearchBinding

class ProfileSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSearchBinding
    private lateinit var firestore: FirebaseFirestore
    private var userId: String? = null

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
}

