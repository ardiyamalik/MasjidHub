package org.d3if0140.masjidhub.ui.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.ui.adapter.AdminPostAdapter
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.ActivityAdminEventBinding
import org.d3if0140.masjidhub.model.Post

class AdminEvent : AppCompatActivity() {
    private lateinit var binding: ActivityAdminEventBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adminPostAdapter: AdminPostAdapter
    private val postList = mutableListOf<Post>()
    private var currentSortOrder: Query.Direction = Query.Direction.DESCENDING // Track the current sort order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi RecyclerView
        adminPostAdapter = AdminPostAdapter(postList) { post -> showDeleteConfirmationDialog(post) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adminPostAdapter

        // Menambahkan onClickListener pada button backButton untuk kembali ke WelcomeActivity
        binding.backButton.setOnClickListener {
            finish()
        }

        // Inisialisasi Spinner
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = spinnerAdapter

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val sortOrder = if (position == 0) Query.Direction.DESCENDING else Query.Direction.ASCENDING
                if (sortOrder != currentSortOrder) {
                    currentSortOrder = sortOrder
                    fetchPosts(sortOrder)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case when no option is selected (if needed)
            }
        }

        // Initial fetch with default sort order (Terbaru)
        fetchPosts(currentSortOrder)
    }

    private fun fetchPosts(sortOrder: Query.Direction) {
        postList.clear() // Clear the current list
        adminPostAdapter.notifyDataSetChanged() // Notify the adapter that the data set has changed

        firestore.collection("posts")
            .orderBy("timestamp", sortOrder)
            .get()
            .addOnSuccessListener { documents ->
                // List to hold posts with user data
                val postsWithUserData = mutableListOf<Post>()

                // Use a batch to fetch user data
                val userFetchTasks = mutableListOf<Task<DocumentSnapshot>>()
                for (document in documents) {
                    val post = document.toObject(Post::class.java).apply {
                        id = document.id  // Set the document ID
                    }
                    // Queue the user data fetch task
                    userFetchTasks.add(
                        firestore.collection("user").document(post.userId).get()
                            .addOnSuccessListener { userDocument ->
                                if (userDocument != null) {
                                    post.nama = userDocument.getString("nama") ?: ""
                                    post.userImageUrl = userDocument.getString("imageUrl") ?: ""
                                    Log.d("AdminEvent", "User data: ${post.nama}, ${post.userImageUrl}")
                                }
                                postsWithUserData.add(post)
                            }
                            .addOnFailureListener { exception ->
                                Log.e("AdminEvent", "Error fetching user data", exception)
                            }
                    )
                }

                // Wait until all user data has been fetched
                Tasks.whenAllSuccess<DocumentSnapshot>(*userFetchTasks.toTypedArray())
                    .addOnSuccessListener {
                        // Update post list and notify adapter
                        postList.addAll(postsWithUserData)
                        adminPostAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("AdminEvent", "Error fetching user data", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("AdminEvent", "Error fetching posts", exception)
            }
    }

    private fun showDeleteConfirmationDialog(post: Post) {
        val input = EditText(this)
        input.hint = "Masukkan alasan penghapusan"

        AlertDialog.Builder(this)
            .setTitle("Hapus Postingan")
            .setMessage("Apakah Anda yakin ingin menghapus postingan ini?")
            .setView(input)
            .setPositiveButton("Hapus") { _, _ ->
                val reason = input.text.toString()
                if (reason.isNotBlank()) {
                    deletePost(post, reason)
                } else {
                    Toast.makeText(this, "Alasan penghapusan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun deletePost(post: Post, reason: String) {
        if (post.id.isNullOrEmpty()) {
            Log.e("AdminEvent", "Post ID is null or empty")
            Toast.makeText(this, "Invalid post ID", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("AdminEvent", "Attempting to delete post with ID: ${post.id}")

        // Delete the post
        firestore.collection("posts").document(post.id)
            .delete()
            .addOnSuccessListener {
                postList.remove(post)
                adminPostAdapter.notifyDataSetChanged()
                Log.d("AdminEvent", "Post deleted successfully: ${post.id}")
                Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show()

                // Save notification to Firestore
                saveNotification(post.userId, reason)
            }
            .addOnFailureListener { exception ->
                Log.e("AdminEvent", "Error deleting post: ${post.id}", exception)
                Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveNotification(userId: String, reason: String) {
        val notification = hashMapOf(
            "userId" to userId,
            "tittle" to "Postingan anda telah di hapus",
            "message" to "Postingan Anda telah dihapus karena: $reason",
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("notifikasi_pengurus_dkm")
            .add(notification)
            .addOnSuccessListener {
                Log.d("AdminEvent", "Notification saved successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("AdminEvent", "Error saving notification", exception)
            }
    }

}
