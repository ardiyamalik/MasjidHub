package org.d3if0140.masjidhub

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.d3if0140.masjidhub.databinding.DialogVerifMasjidBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast

class VerifikasiMasjidActivity : AppCompatActivity() {

    private lateinit var binding: DialogVerifMasjidBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogVerifMasjidBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Ambil data masjid yang dikirimkan dari Intent
        val masjidName = intent.getStringExtra("NAMA_MASJID") ?: "Tidak diketahui"
        val alamat = intent.getStringExtra("ALAMAT") ?: "Alamat tidak tersedia"
        val kodePos = intent.getStringExtra("KODE_POS") ?: "Kode Pos tidak tersedia"
        val teleponMasjid = intent.getStringExtra("TELEPON_MASJID") ?: "Telepon tidak tersedia"
        val namaKetua = intent.getStringExtra("NAMA_KETUA") ?: "Nama Ketua tidak tersedia"
        val teleponKetua = intent.getStringExtra("TELEPON_KETUA") ?: "Telepon Ketua tidak tersedia"
        val email = intent.getStringExtra("EMAIL") ?: "Email tidak tersedia"
        val latitude = intent.getStringExtra("LATITUDE") ?: "Tidak tersedia"
        val longitude = intent.getStringExtra("LONGITUDE") ?: "Tidak tersedia"
        val ktpUrl = intent.getStringExtra("KTP_URL") ?: ""

        // Setel data ke TextView dan ImageView
        binding.textViewMasjidName.text = masjidName
        binding.textViewAlamat.text = alamat
        binding.textViewKodePos.text = kodePos
        binding.textViewTeleponMasjid.text = teleponMasjid
        binding.textViewNamaKetua.text = namaKetua
        binding.textViewTeleponKetua.text = teleponKetua
        binding.textViewEmail.text = email
        binding.textViewLatitude.text = latitude
        binding.textViewLongitude.text = longitude

        if (ktpUrl.isNotEmpty()) {
            binding.imageViewKtp.visibility = View.VISIBLE
            Glide.with(this)
                .load(ktpUrl)
                .into(binding.imageViewKtp)

            binding.imageViewKtp.setOnClickListener {
                val intent = Intent(this, FullScreenImageActivity::class.java).apply {
                    putExtra("IMAGE_URL", ktpUrl)
                }
                startActivity(intent)
            }
        } else {
            binding.imageViewKtp.visibility = View.GONE
        }

        // Menggunakan ViewBinding untuk icon close
        binding.closeIcon.setOnClickListener {
            finish()
        }

        // Setel tombol Batal
        binding.buttonCancel.setOnClickListener {
            finish() // Tutup activity ini
        }

        // Setel tombol Verifikasi
        binding.buttonVerify.setOnClickListener {
            verifyMasjid(masjidName)
        }
    }

    private fun verifyMasjid(namaMasjid: String) {
        firestore.collection("user")
            .whereEqualTo("nama", namaMasjid)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents.first()
                    val masjidId = doc.id
                    firestore.collection("user")
                        .document(masjidId)
                        .update("verified", true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Masjid berhasil diverifikasi", Toast.LENGTH_SHORT)
                                .show()
                            setResult(RESULT_OK) // Set result OK
                            finish() // Tutup activity setelah verifikasi selesai
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Gagal memverifikasi masjid: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(this, "Masjid tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mencari masjid: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
