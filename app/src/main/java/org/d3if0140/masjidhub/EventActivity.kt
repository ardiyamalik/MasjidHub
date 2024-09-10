package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi RecyclerView
        postAdapter = PostAdapter(postList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = postAdapter

        binding.backButton.setOnClickListener {
            finish()
        }

        // Setup Spinner for filtering
        val filterOptions = resources.getStringArray(R.array.filter_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filterSpinner.adapter = adapter

        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedFilter = filterOptions[position]
                loadPosts(selectedFilter)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optionally handle the case when nothing is selected
            }
        }

        // Initial load
        loadPosts(filterOptions[0]) // Load posts with the default filter
    }

    private fun loadPosts(filter: String) {
        postList.clear() // Clear the current list

        val direction = if (filter == "Terbaru") {
            Query.Direction.DESCENDING
        } else {
            Query.Direction.ASCENDING
        }

        firestore.collection("posts")
            .orderBy("timestamp", direction)
            .get()
            .addOnSuccessListener { documents ->
                // List to hold posts with user data
                val postsWithUserData = mutableListOf<Post>()

                // Use a batch to fetch user data
                val userFetchTasks = mutableListOf<Task<DocumentSnapshot>>()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    // Queue the user data fetch task
                    userFetchTasks.add(
                        firestore.collection("user").document(post.userId).get()
                            .addOnSuccessListener { userDocument ->
                                if (userDocument != null) {
                                    post.nama = userDocument.getString("nama") ?: ""
                                    post.userImageUrl = userDocument.getString("imageUrl") ?: ""
                                    Log.d("EventActivity", "User data: ${post.nama}, ${post.userImageUrl}")
                                }
                                postsWithUserData.add(post)
                            }
                            .addOnFailureListener { exception ->
                                Log.e("EventActivity", "Error fetching user data", exception)
                            }
                    )
                }

                // Wait until all user data has been fetched
                Tasks.whenAllSuccess<DocumentSnapshot>(*userFetchTasks.toTypedArray())
                    .addOnSuccessListener {
                        postList.addAll(postsWithUserData)
                        postAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("EventActivity", "Error fetching user data", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("EventActivity", "Error fetching posts", exception)
            }
    }
}

