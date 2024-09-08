package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InfaqFragment : Fragment() {

    private lateinit var qrImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var norekTextView: TextView
    private lateinit var uploadButton: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_infaq, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        qrImageView = view.findViewById(R.id.qrImageView)
        nameTextView = view.findViewById(R.id.nameTextView)
        norekTextView = view.findViewById(R.id.norekTextView)
        uploadButton = view.findViewById(R.id.uploadButton)

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Fetch data from Firestore
        fetchUserData()

        // Set click listener for the upload button
//        uploadButton.setOnClickListener {
//            val intent = Intent(activity, UploadActivity::class.java)
//            startActivity(intent)
//        }
    }

    private fun fetchUserData() {
        val currentUserId = mAuth.currentUser?.uid
        if (currentUserId != null) {
            firestore.collection("user")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userData = document.data
                        val name = userData?.get("nama") as? String
                        val norek = userData?.get("norek") as? String
                        val qrImageUrl = userData?.get("qrImageUrl") as? String

                        // Set data to views
                        nameTextView.text = name ?: "Nama tidak tersedia"
                        norekTextView.text = norek ?: "Norek tidak tersedia"
                        qrImageUrl?.let {
                            Glide.with(this)
                                .load(it)
                                .placeholder(R.drawable.baseline_person_black) // Set a placeholder image if needed
                                .into(qrImageView)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
    }
}
