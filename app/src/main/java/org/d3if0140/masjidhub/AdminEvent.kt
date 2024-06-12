package org.d3if0140.masjidhub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.databinding.ActivityAdminEventBinding

class AdminEvent : AppCompatActivity() {
    private lateinit var binding: ActivityAdminEventBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adminPostAdapter: AdminPostAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menambahkan onClickListener pada button backButton untuk kembali ke WelcomeActivity
        binding.backButton.setOnClickListener {
            finish()
        }

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi RecyclerView
        adminPostAdapter = AdminPostAdapter(postList) { post -> deletePost(post) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adminPostAdapter

        // Ambil data postingan dari Firestore dengan urutan terbaru ke lama
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val post = document.toObject(Post::class.java).apply {
                        id = document.id  // Set the document ID
                    }
                    // Ambil data user berdasarkan userId dari postingan
                    firestore.collection("user").document(post.userId)
                        .get()
                        .addOnSuccessListener { userDocument ->
                            if (userDocument != null) {
                                post.nama = userDocument.getString("nama") ?: ""
                                post.userImageUrl = userDocument.getString("imageUrl") ?: ""
                                Log.d(
                                    "AdminEvent",
                                    "User data: ${post.nama}, ${post.userImageUrl}"
                                )
                            }
                            postList.add(post)
                            adminPostAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("AdminEvent", "Error fetching user data", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AdminEvent", "Error fetching posts", exception)
            }
    }

    private fun deletePost(post: Post) {
        // Hapus dari Firestore menggunakan ID dokumen yang benar
        firestore.collection("posts").document(post.id)
            .delete()
            .addOnSuccessListener {
                // Hapus dari postList dan beri tahu adapter
                postList.remove(post)
                adminPostAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("AdminEvent", "Error deleting post", exception)
                Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show()
            }
    }
}
