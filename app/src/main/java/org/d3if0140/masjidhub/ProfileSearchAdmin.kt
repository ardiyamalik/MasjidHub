package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.databinding.ActivityProfileSearchAdminBinding

class ProfileSearchAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSearchAdminBinding
    private lateinit var firestore: FirebaseFirestore
    private val postList = mutableListOf<Post>()
    private lateinit var adminPostAdapter: AdminPostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSearchAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Setup RecyclerView for posts
        adminPostAdapter = AdminPostAdapter(postList) { post ->
            deletePost(post)
        }
        binding.recyclerViewPost.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPost.adapter = adminPostAdapter

        // Get userId from Intent
        val userId = intent.getStringExtra("USER_ID")

        // Logging userId for debugging
        Log.d("ProfileSearchAdmin", "Received userId: $userId")

        if (userId != null && userId.isNotEmpty()) {
            // Load user profile data
            loadUserProfile(userId)
            // Load posts related to the user
            loadPosts(userId)
        } else {
            Log.e("ProfileSearchAdmin", "Invalid or missing userId")
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
        }

        binding.backButton.setOnClickListener {
            finish() // Kembali ke aktivitas sebelumnya
        }

        binding.jamaahYangTerdaftar.setOnClickListener {
            val intent = Intent(this, AdminJamaahTerdaftar::class.java)
            intent.putExtra("nama", binding.namaUserDkm.text.toString())
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

    private fun loadPosts(userId: String) {
        Log.d("ProfileSearchAdmin", "Loading posts for userId: $userId")

        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                Log.d("ProfileSearchAdmin", "Found ${documents.size()} posts")
                for (document in documents) {
                    val post = document.toObject(Post::class.java).apply {
                        id = document.id // Menambahkan ID dokumen ke objek Post
                        nama = binding.namaUserDkm.text.toString() // Menambahkan nama pengguna ke Post
                        userImageUrl = document.getString("imageUrl") ?: ""
                    }
                    postList.add(post)
                }
                adminPostAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileSearchAdmin", "Error fetching posts", e)
                Toast.makeText(this, "Error fetching posts: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileImage(imageUrl: String, imageView: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.baseline_arrow_back)
            .into(imageView)
    }

    private fun deletePost(post: Post) {
        firestore.collection("posts").document(post.id)
            .delete()
            .addOnSuccessListener {
                postList.remove(post)
                adminPostAdapter.notifyDataSetChanged()
                Log.d("ProfileSearchAdmin", "Post deleted successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileSearchAdmin", "Error deleting post: ", exception)
            }
    }
}
