package org.d3if0140.masjidhub

import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
        adminPostAdapter = AdminPostAdapter(postList) { post -> showDeleteConfirmationDialog(post) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adminPostAdapter

        // Inisialisasi Spinner
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = spinnerAdapter

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val sortOrder = if (position == 0) Query.Direction.DESCENDING else Query.Direction.ASCENDING
                fetchPosts(sortOrder)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case when no option is selected (if needed)
            }
        }

        // Initial fetch with default sort order (Terbaru)
        fetchPosts(Query.Direction.DESCENDING)
    }

    private fun fetchPosts(sortOrder: Query.Direction) {
        postList.clear()
        adminPostAdapter.notifyDataSetChanged()

        firestore.collection("posts")
            .orderBy("timestamp", sortOrder)
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

    private fun showDeleteConfirmationDialog(post: Post) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Postingan")
            .setMessage("Apakah Anda yakin ingin menghapus postingan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deletePost(post)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deletePost(post: Post) {
        if (post.id.isNullOrEmpty()) {
            Log.e("AdminEvent", "Post ID is null or empty")
            Toast.makeText(this, "Invalid post ID", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("AdminEvent", "Attempting to delete post with ID: ${post.id}")
        firestore.collection("posts").document(post.id)
            .delete()
            .addOnSuccessListener {
                postList.remove(post)
                adminPostAdapter.notifyDataSetChanged()
                Log.d("AdminEvent", "Post deleted successfully: ${post.id}")
                Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("AdminEvent", "Error deleting post: ${post.id}", exception)
                Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show()
            }
    }
}