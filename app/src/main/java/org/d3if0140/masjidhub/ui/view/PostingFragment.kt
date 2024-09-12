package org.d3if0140.masjidhub.ui.view

import android.app.ProgressDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.ui.adapter.DkmPostAdapter
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.FragmentPostingBinding
import org.d3if0140.masjidhub.model.Post

class PostingFragment : Fragment() {

    private lateinit var binding: FragmentPostingBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dkmPostAdapter: DkmPostAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        dkmPostAdapter = DkmPostAdapter(postList, { post -> showEditCaptionDialog(post) }, { post -> showDeleteConfirmationDialog(post) })
        binding.recyclerViewPost.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPost.adapter = dkmPostAdapter

        // Load posts from Firestore
        loadPosts()

        // Setup TabLayout listener if needed
        // (this part is usually handled in the Activity hosting the Fragment)
    }

    private fun loadPosts() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            firestore.collection("posts")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
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
                                    dkmPostAdapter.notifyDataSetChanged()
                                }
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PostingFragment", "Gagal mengambil postingan: ${exception.message}", exception)
                    Toast.makeText(requireContext(), "Gagal mengambil postingan: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showEditCaptionDialog(post: Post) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_caption, null)
        val captionEditText: EditText = dialogView.findViewById(R.id.captionEditText)
        captionEditText.setText(post.deskripsi)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Caption")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val newCaption = captionEditText.text.toString()
                if (newCaption.isNotBlank()) {
                    updateCaption(post, newCaption)
                } else {
                    Toast.makeText(requireContext(), "Caption tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateCaption(post: Post, newCaption: String) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Mengupdate caption...")
        progressDialog.show()

        val postRef = firestore.collection("posts").document(post.id)
        postRef.update("deskripsi", newCaption)
            .addOnSuccessListener {
                progressDialog.dismiss()
                post.deskripsi = newCaption
                dkmPostAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Caption updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Log.e("PostingFragment", "Failed to update caption for post ID: ${post.id}", exception)
                Toast.makeText(requireContext(), "Gagal mengupdate caption: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmationDialog(post: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Postingan")
            .setMessage("Apakah Anda yakin ingin menghapus postingan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deletePost(post)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deletePost(post: Post) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Menghapus postingan...")
        progressDialog.show()

        if (post.id.isNullOrEmpty()) {
            Log.e("PostingFragment", "Post ID is null or empty")
            Toast.makeText(requireContext(), "Invalid post ID", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("posts").document(post.id)
            .delete()
            .addOnSuccessListener {
                progressDialog.dismiss()
                postList.remove(post)
                dkmPostAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to delete post", Toast.LENGTH_SHORT).show()
            }
    }
}
