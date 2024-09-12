package org.d3if0140.masjidhub.ui.view

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.FragmentInfaqAdminBinding
import java.io.OutputStream

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
        // Set download QR button listener
        binding.downloadQrButton.setOnClickListener {
            downloadQrImage()
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

    private fun downloadQrImage() {
        val drawable = binding.qrImageView.drawable as? BitmapDrawable
        val bitmap = drawable?.bitmap
        if (bitmap != null) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "QR_Code_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QR Codes")
            }

            val resolver = requireActivity().contentResolver
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            val outputStream: OutputStream? = imageUri?.let { resolver.openOutputStream(it) }

            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Toast.makeText(context, "QR berhasil diunduh", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Gambar QR tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }
}
