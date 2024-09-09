package org.d3if0140.masjidhub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.FragmentInfaqAdminBinding

class InfaqAdminFragment : Fragment() {

    private lateinit var binding: FragmentInfaqAdminBinding
    private lateinit var firestore: FirebaseFirestore
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfaqAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // Get userId from arguments
        userId = arguments?.getString("USER_ID")
        userId?.let {
            loadInfaqData(it)
        }
    }

    private fun loadInfaqData(userId: String) {
        firestore.collection("user").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val namaRekening = document.getString("namaRekening") ?: "Unknown"
                    val nomorRekening = document.getString("nomorRekening") ?: "Unknown"
                    val namaBank = document.getString("namaBank") ?: "Unknown"
                    val qrImageUrl = document.getString("qrImageUrl")

                    binding.namaRekening.text = namaRekening
                    binding.norekTextView.text = nomorRekening
                    binding.namaBank.text = namaBank

                    // Load QR image using Glide
                    Glide.with(this)
                        .load(qrImageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .into(binding.qrImageView)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading infaq data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
