package org.d3if0140.masjidhub

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityEventBinding

class EventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menambahkan onClickListener pada button backButton untuk kembali ke WelcomeActivity
        binding.backButton.setOnClickListener {
            finish()
        }

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi RecyclerView
        postAdapter = PostAdapter(postList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = postAdapter

        // Ambil ID pengguna saat ini
        val currentUserId = mAuth.currentUser?.uid

        // Ambil data postingan dari Firestore
        firestore.collection("posts")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    // Ambil data user berdasarkan userId dari postingan
                    firestore.collection("user").document(post.userId)
                        .get()
                        .addOnSuccessListener { userDocument ->
                            if (userDocument != null) {
                                post.nama = userDocument.getString("nama") ?: ""
                                post.userImageUrl = userDocument.getString("userImageUrl") ?: ""
                                Log.d(
                                    "EventActivity",
                                    "User data: ${post.nama}, ${post.userImageUrl}"
                                )
                            }
                            postList.add(post)
                            postAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("EventActivity", "Error fetching user data", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EventActivity", "Error fetching posts", exception)
            }
    }
}

