package org.d3if0140.masjidhub

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.FragmentInfaqSearchBinding
import java.io.OutputStream

class InfaqSearchFragment : Fragment() {

    private lateinit var binding: FragmentInfaqSearchBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfaqSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Fetch user data
        fetchUserData()

        // Set download QR button listener
        binding.downloadQrButton.setOnClickListener {
            downloadQrImage()
        }
    }

    private fun fetchUserData() {
        val selectedUserId = arguments?.getString("USER_ID")
        if (selectedUserId != null) {
            firestore.collection("user")
                .document(selectedUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userData = document.data
                        val name = userData?.get("nama") as? String
                        val namaRekening = userData?.get("namaRekening") as? String
                        val norek = userData?.get("nomorRekening") as? String
                        val namaBank = userData?.get("namaBank") as? String
                        val qrImageUrl = userData?.get("qrImageUrl") as? String

                        // Set data to views
                        binding.nameTextView.text = name ?: "Nama tidak tersedia"
                        binding.namaRekening.text = namaRekening ?: "Nama Rekening tidak tersedia"
                        binding.norekTextView.text = norek ?: "Norek tidak tersedia"
                        binding.namaBank.text = namaBank ?: "Nama Bank tidak tersedia"
                        qrImageUrl?.let {
                            Glide.with(this)
                                .load(it)
                                .placeholder(R.drawable.baseline_person_black)
                                .into(binding.qrImageView)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
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

