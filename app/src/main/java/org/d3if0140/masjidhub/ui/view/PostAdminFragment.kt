package org.d3if0140.masjidhub.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.ui.adapter.AdminPostAdapter
import org.d3if0140.masjidhub.databinding.FragmentPostAdminBinding
import org.d3if0140.masjidhub.model.Post

class PostAdminFragment : Fragment() {

    private lateinit var binding: FragmentPostAdminBinding
    private lateinit var firestore: FirebaseFirestore
    private val postList = mutableListOf<Post>()
    private lateinit var postAdapter: AdminPostAdapter
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        postAdapter = AdminPostAdapter(postList) { post -> showDeleteConfirmationDialog(post) }
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
                    val post = document.toObject(Post::class.java).apply {
                        id = document.id
                    }
                    // Load user profile data for each post
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
                    // Update the post list only after getting the profile data
                    if (!postList.contains(post)) {
                        postList.add(post)
                        postAdapter.notifyDataSetChanged()
                    }
                } else {
                    Log.e("PostAdminFragment", "User document not found for userId: ${post.userId}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("PostAdminFragment", "Error fetching user document", e)
            }
    }

    private fun showDeleteConfirmationDialog(post: Post) {
        val input = EditText(requireContext())
        input.hint = "Masukkan alasan penghapusan"

        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Postingan")
            .setMessage("Apakah Anda yakin ingin menghapus postingan ini?")
            .setView(input)
            .setPositiveButton("Hapus") { dialog, _ ->
                val reason = input.text.toString()
                if (reason.isNotBlank()) {
                    deletePost(post, reason)
                } else {
                    Toast.makeText(requireContext(), "Alasan penghapusan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deletePost(post: Post, reason: String) {
        if (post.id.isNullOrEmpty()) {
            Log.e("PostAdminFragment", "Post ID is null or empty")
            Toast.makeText(requireContext(), "Invalid post ID", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("posts").document(post.id)
            .delete()
            .addOnSuccessListener {
                postList.remove(post)
                postAdapter.notifyDataSetChanged()
                Log.d("PostAdminFragment", "Post deleted successfully")
                Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()

                // Save notification to Firestore
                saveNotification(post.userId, reason)
            }
            .addOnFailureListener { e ->
                Log.e("PostAdminFragment", "Error deleting post", e)
                Toast.makeText(requireContext(), "Failed to delete post", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveNotification(userId: String, reason: String) {
        val notification = hashMapOf(
            "userId" to userId,
            "message" to "Postingan Anda telah dihapus karena: $reason",
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("notifikasi_pengurus_dkm")
            .add(notification)
            .addOnSuccessListener {
                Log.d("PostAdminFragment", "Notification saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("PostAdminFragment", "Error saving notification", e)
            }
    }
}
