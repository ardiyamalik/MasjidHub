package org.d3if0140.masjidhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.ActivityInfaqBinding

class InfaqActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfaqBinding
    private val db = FirebaseFirestore.getInstance()

    private val getImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.imageViewBuktiPembayaran.setImageURI(it)
            binding.imageViewBuktiPembayaran.visibility = android.view.View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonPilihFoto.setOnClickListener {
            getImageFromGallery.launch("image/*")
        }

        binding.radioGroupMetode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonBankTransfer -> {
                    binding.textViewRekening.visibility = android.view.View.VISIBLE
                    binding.imageViewQR.visibility = android.view.View.GONE
                }
                R.id.radioButtonQR -> {
                    binding.textViewRekening.visibility = android.view.View.GONE
                    binding.imageViewQR.visibility = android.view.View.VISIBLE
                }
            }
        }

        binding.buttonBayar.setOnClickListener {
            val jumlahInfaq = binding.editTextJumlahInfaq.text.toString().toDoubleOrNull()
            val metodePembayaran = when {
                binding.radioButtonBankTransfer.isChecked -> "Bank Transfer"
                binding.radioButtonQR.isChecked -> "QR"
                else -> null
            }

            if (jumlahInfaq != null && metodePembayaran != null) {
                val infaqData = hashMapOf(
                    "jumlahInfaq" to jumlahInfaq,
                    "metodePembayaran" to metodePembayaran,
                    "buktiPembayaran" to binding.imageViewBuktiPembayaran.drawable.toString()
                )

                db.collection("infaq_masjid")
                    .add(infaqData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Infaq berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, KeuanganActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal menyimpan infaq: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Harap lengkapi form pembayaran", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
