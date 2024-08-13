package org.d3if0140.masjidhub

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.d3if0140.masjidhub.databinding.ActivityProfilDkmBinding

class ProfilDkmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfilDkmBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dkmPostAdapter: DkmPostAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilDkmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        dkmPostAdapter = DkmPostAdapter(postList, { post -> showEditCaptionDialog(post) }, { post -> showDeleteConfirmationDialog(post) })
        binding.recyclerViewPost.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPost.adapter = dkmPostAdapter

        // Atur listener untuk tombol logout
        binding.buttonLogoutDkm.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Apakah Anda yakin ingin logout?")
                .setCancelable(false)
                .setPositiveButton("Ya") { dialog, id ->
                    mAuth.signOut()
                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Tidak") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        // Atur listener untuk tombol ubah profil
        binding.ubahProfileDkm.setOnClickListener {
            val intent = Intent(this, UbahProfilDkm::class.java)
            startActivity(intent)
        }

        binding.jamaahYangTerdaftar.setOnClickListener {
            val intent = Intent(this, JamaahTerdaftar::class.java)
            intent.putExtra("nama", binding.namaUserDkm.text.toString())
            startActivity(intent)
        }


        // Dapatkan ID pengguna yang saat ini masuk
        val currentUserId = mAuth.currentUser?.uid

        // Ambil data pengguna dari Firestore berdasarkan ID
        if (currentUserId != null) {
            firestore.collection("user")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userData = document.data
                        if (userData != null) {
                            val nama = userData["nama"] as? String
                            val alamat = userData["alamat"] as? String
                            val imageUrl = userData["imageUrl"] as? String

                            // Tampilkan nama pengguna jika tidak null
                            nama?.let { binding.namaUserDkm.text = it }

                            // Tampilkan alamat masjid
                            alamat?.let { binding.alamatMasjid.text = it }

                            // Tampilkan foto profil jika URL tidak null
                            imageUrl?.let { loadProfileImage(it) } ?: run { displayDefaultProfileImage() }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal mengambil data pengguna: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Load posts dari user yang sedang login
        loadPosts(currentUserId)

        // Bottom navigation listener
        binding.bottomNavigationDkm.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile_dkm -> true
                R.id.menu_home_dkm -> {
                    val intent = Intent(this, DkmDashboard::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_finance -> {
                    val intent = Intent(this, KeuanganDkmActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.unggahEvent -> {
                    val intent = Intent(this, UnggahActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadProfileImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(createAvatar(mAuth.currentUser?.email ?: ""))
            .into(binding.profileImageDkm)
    }

    private fun displayDefaultProfileImage() {
        val user = mAuth.currentUser
        if (user != null && user.email != null) {
            val email = user.email
            email?.let {
                val avatarDrawable = createAvatar(it)
                binding.profileImageDkm.setImageDrawable(avatarDrawable)
            }
        }
    }

    private fun createAvatar(email: String): Drawable {
        val color = getColorFromEmail(email)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { this.color = color }
        canvas.drawCircle(50f, 50f, 50f, paint)
        val textPaint = Paint().apply {
            this.color = Color.WHITE
            this.textSize = 40f
            this.textAlign = Paint.Align.CENTER
        }
        val initial = email.substring(0, 1).uppercase()
        canvas.drawText(initial, 50f, 65f, textPaint)
        return BitmapDrawable(resources, bitmap)
    }

    private fun getColorFromEmail(email: String): Int {
        val hashCode = email.hashCode()
        return Color.HSVToColor(floatArrayOf(
            (hashCode and 0xFF).toFloat() % 360,
            0.6f,
            0.9f
        ))
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
                                dkmPostAdapter.notifyDataSetChanged()
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfilDkmActivity", "Gagal mengambil postingan: ${exception.message}", exception)
                Toast.makeText(this, "Gagal mengambil postingan: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditCaptionDialog(post: Post) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_caption, null)
        val captionEditText: EditText = dialogView.findViewById(R.id.captionEditText)
        captionEditText.setText(post.deskripsi)

        Log.d("showEditCaptionDialog", "Showing dialog for post ID: ${post.id}")

        AlertDialog.Builder(this)
            .setTitle("Edit Caption")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val newCaption = captionEditText.text.toString()
                if (newCaption.isNotBlank()) {
                    updateCaption(post, newCaption)
                } else {
                    Toast.makeText(this, "Caption tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateCaption(post: Post, newCaption: String) {
        val postRef = firestore.collection("posts").document(post.id)
        Log.d("updateCaption", "Updating post ID: ${post.id} with new caption: $newCaption")

        postRef.update("deskripsi", newCaption)
            .addOnSuccessListener {
                post.deskripsi = newCaption
                dkmPostAdapter.notifyDataSetChanged()
                Log.d("updateCaption", "Caption updated successfully for post ID: ${post.id}")
                Toast.makeText(this, "Caption updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("updateCaption", "Failed to update caption for post ID: ${post.id}", exception)
                Toast.makeText(this, "Gagal mengupdate caption: ${exception.message}", Toast.LENGTH_SHORT).show()
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
                dkmPostAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show()
            }
    }
}
