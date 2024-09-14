package org.d3if0140.masjidhub.ui.view

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.DialogVerifMasjidBinding

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
        val latitude = intent.getStringExtra("LATITUDE")?.toDoubleOrNull() ?: 0.0
        val longitude = intent.getStringExtra("LONGITUDE")?.toDoubleOrNull() ?: 0.0
        val ktpUrl = intent.getStringExtra("KTP_URL") ?: ""

        // Fungsi untuk membuat bagian dari teks menjadi bold
        fun makeBold(text: String, label: String): SpannableString {
            val spannableString = SpannableString(label + text)
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                label.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannableString
        }

        // Setel data ke TextView dan ImageView
        binding.textViewMasjidName.text = makeBold(masjidName, "Nama: ")
        binding.textViewAlamat.text = makeBold(alamat, "Alamat: ")
        binding.textViewKodePos.text = makeBold(kodePos, "Kode Pos: ")
        binding.textViewTeleponMasjid.text = makeBold(teleponMasjid, "Telepon Masjid: ")
        binding.textViewNamaKetua.text = makeBold(namaKetua, "Nama Ketua: ")
        binding.textViewTeleponKetua.text = makeBold(teleponKetua, "Telepon Ketua: ")
        binding.textViewEmail.text = makeBold(email, "Email: ")
        binding.textViewLatitude.text = makeBold(latitude.toString(), "Titik Koordinat: ")
        binding.textViewLongitude.text = makeBold(longitude.toString(), "Titik Koordinat: ")

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
            verifyMasjid(masjidName, latitude, longitude)
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radius Bumi dalam kilometer
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun verifyMasjid(namaMasjid: String, latitude: Double, longitude: Double) {
        firestore.collection("user")
            .get()
            .addOnSuccessListener { result ->
                var isNearby = false
                for (document in result.documents) {
                    val docLatitude = document.getDouble("latitude") ?: continue
                    val docLongitude = document.getDouble("longitude") ?: continue

                    val distance = calculateDistance(latitude, longitude, docLatitude, docLongitude)
                    if (distance < 0.5) { // Jarak dalam kilometer (misalnya, 0.5 km)
                        isNearby = true
                        break
                    }
                }

                if (isNearby) {
                    showConfirmationDialog(namaMasjid, latitude, longitude)
                } else {
                    // Proses verifikasi jika lokasi tidak ada yang sama atau dekat
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
                                        Toast.makeText(this, "Masjid berhasil diverifikasi", Toast.LENGTH_SHORT).show()
                                        setResult(RESULT_OK) // Set result OK
                                        finish() // Tutup activity setelah verifikasi selesai
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Gagal memverifikasi masjid: ${e.message}", Toast.LENGTH_SHORT).show()
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
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mencari masjid: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showConfirmationDialog(namaMasjid: String, latitude: Double, longitude: Double) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Konfirmasi Verifikasi")
            .setMessage("Titik lokasi masjid ini sangat dekat dengan lokasi masjid lain. Apakah Anda yakin ingin memverifikasi masjid ini?")
            .setPositiveButton("OK") { _, _ ->
                // Lanjutkan proses verifikasi
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
                                    Toast.makeText(this, "Masjid berhasil diverifikasi", Toast.LENGTH_SHORT).show()
                                    setResult(RESULT_OK) // Set result OK
                                    finish() // Tutup activity setelah verifikasi selesai
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Gagal memverifikasi masjid: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Masjid tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal mencari masjid: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss() // Tutup dialog
            }
            .create()
        dialog.show()
    }
}
