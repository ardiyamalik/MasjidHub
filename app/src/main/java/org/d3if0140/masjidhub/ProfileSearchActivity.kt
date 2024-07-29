package org.d3if0140.masjidhub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.databinding.ActivityProfileSearchBinding

class ProfileSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSearchBinding
    private lateinit var firestore: FirebaseFirestore
    private val postList = mutableListOf<Post>()
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Setup RecyclerView for posts
        postAdapter = PostAdapter(postList)
        binding.recyclerViewPost.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPost.adapter = postAdapter

        // Get data from Intent
        val userId = intent.getStringExtra("USER_ID")
        val userName = intent.getStringExtra("USER_NAME")
        val userAlamat = intent.getStringExtra("USER_ALAMAT")
        val userImageUrl = intent.getStringExtra("USER_IMAGE_URL")

        binding.namaUserDkm.text = userName
        binding.alamatMasjid.text = userAlamat
        if (userImageUrl != null) {
            Glide.with(this)
                .load(userImageUrl)
                .into(binding.profileImageDkm)
        } else {
            binding.profileImageDkm.setImageResource(R.drawable.baseline_person_black)
        }

        // Load posts related to the mosque
        userId?.let { loadPosts(it) }
    }

    private fun loadPosts(userId: String?) {
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    post.id = document.id // Menambahkan ID dokument ke objek Post
                    firestore.collection("user").document(post.userId)
                        .get()
                        .addOnSuccessListener { userDocument ->
                            if (userDocument != null) {
                                post.nama = userDocument.getString("nama") ?: "Unknown User"
                                post.userImageUrl = userDocument.getString("imageUrl") ?: ""
                                postList.add(post)
                                postAdapter.notifyDataSetChanged()
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileSearchActivity", "Error fetching posts", e)
                Toast.makeText(this, "Error fetching posts: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

