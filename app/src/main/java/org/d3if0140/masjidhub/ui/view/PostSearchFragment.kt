package org.d3if0140.masjidhub.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.ui.adapter.PostAdapter
import org.d3if0140.masjidhub.databinding.FragmentPostSearchBinding
import org.d3if0140.masjidhub.model.Post

class PostSearchFragment : Fragment() {

    private lateinit var binding: FragmentPostSearchBinding
    private lateinit var firestore: FirebaseFirestore
    private val postList = mutableListOf<Post>()
    private lateinit var postAdapter: PostAdapter
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        postAdapter = PostAdapter(postList)
        binding.recyclerViewPost.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPost.adapter = postAdapter

        // Get userId from arguments
        userId = arguments?.getString("USER_ID")
        userId?.let {
            loadPosts(it)
        }
    }

    private fun loadPosts(userId: String) {
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    post.id = document.id
                    loadUserProfileData(post)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching posts: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfileData(post: Post) {
        firestore.collection("user").document(post.userId)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    post.nama = userDocument.getString("nama") ?: "Unknown User"
                    post.userImageUrl = userDocument.getString("imageUrl") ?: ""
                    postList.add(post)
                    postAdapter.notifyDataSetChanged()
                } else {
                    Log.e("PostDkmFragment", "User document not found for userId: ${post.userId}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("PostDkmFragment", "Error fetching user document", e)
            }
    }
}
