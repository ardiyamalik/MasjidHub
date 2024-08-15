package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityCariMasjidBinding

class CariMasjidActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCariMasjidBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCariMasjidBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        userAdapter = UserAdapter(userList) { user ->
            val intent = Intent(this, ProfileSearchActivity::class.java).apply {
                putExtra("USER_ID", user.userId)
                putExtra("USER_NAME", user.nama)
                putExtra("USER_ALAMAT", user.alamat)
                putExtra("USER_IMAGE_URL", user.imageUrl)
            }
            Log.d("CariMasjidActivity", "Sending userId: ${user.userId}, userImageUrl: ${user.imageUrl}")
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = userAdapter

        val editTextSearch: EditText = binding.editTextSearch
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.bottomNavigation.selectedItemId = R.id.search_masjid
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search_masjid -> true
                R.id.menu_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_finance -> {
                    startActivity(Intent(this, KeuanganActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfilActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun searchUsers(query: String) {
        Log.d("CariMasjidActivity", "Searching users with query: $query")

        firestore.collection("user")
            .whereEqualTo("role", "pengurus_dkm")
            .orderBy("nama")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("CariMasjidActivity", "Successfully fetched users")
                userList.clear()
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    user.userId = document.id // Tambahkan ini jika userId tidak ada di dalam objek User
                    Log.d("CariMasjidActivity", "User found: ${user.nama}, ID: ${document.id}, Image URL: ${user.imageUrl}")
                    userList.add(user)
                }
                userAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.w("CariMasjidActivity", "Error fetching users", e)
                Toast.makeText(this, "Error fetching users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
