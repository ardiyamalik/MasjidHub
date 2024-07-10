package org.d3if0140.masjidhub

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.d3if0140.masjidhub.databinding.ActivityPengisianKasBinding
import java.util.*
import java.util.concurrent.TimeUnit

class PengisianKasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPengisianKasBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null
    private val TAG = "PengisianKasActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengisianKasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengatur countdown timer selama 7 hari
        val countdownMillis = TimeUnit.DAYS.toMillis(7)
        val textViewCountdown = binding.textViewCountdown
        object : CountDownTimer(countdownMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished % TimeUnit.DAYS.toMillis(1))
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished % TimeUnit.HOURS.toMillis(1))
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished % TimeUnit.MINUTES.toMillis(1))

                textViewCountdown.text = String.format("%02d days %02d:%02d:%02d", days, hours, minutes, seconds)
            }

            override fun onFinish() {
                textViewCountdown.text = "Countdown finished"
            }
        }.start()

        binding.radioGroupMetode.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = findViewById(checkedId)
            if (radioButton.text == "Bank Transfer") {
                binding.textViewRekening.visibility = android.view.View.VISIBLE
                binding.imageViewQR.visibility = android.view.View.GONE
            } else {
                binding.textViewRekening.visibility = android.view.View.GONE
                binding.imageViewQR.visibility = android.view.View.VISIBLE
            }
        }

        binding.buttonPilihFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        binding.buttonBayar.setOnClickListener {
            submitKas()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            imageUri = data?.data
            binding.imageViewBuktiPembayaran.setImageURI(imageUri)
            binding.imageViewBuktiPembayaran.visibility = android.view.View.VISIBLE
        }
    }

    private fun submitKas() {
        val jumlah = 50000.0 // Jumlah kas tetap
        val metode = when (binding.radioGroupMetode.checkedRadioButtonId) {
            R.id.radioButtonBankTransfer -> "Bank Transfer"
            R.id.radioButtonQR -> "QR"
            else -> ""
        }

        if (metode.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Harap isi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val kasId = UUID.randomUUID().toString()

        val kasData = hashMapOf(
            "jumlah" to jumlah,
            "metode" to metode,
            "status" to "pending",
            "userId" to userId,
            "timestamp" to System.currentTimeMillis(),
            "buktiPembayaranUrl" to "" // Tambahkan ini untuk bukti pembayaran URL
        )

        db.collection("kas_mingguan").document(kasId)
            .set(kasData)
            .addOnSuccessListener {
                Log.d(TAG, "Document successfully written!")
                uploadBuktiPembayaran(kasId)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(this, "Gagal mengirim kas", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error adding document", e)
            }
    }


    private fun uploadBuktiPembayaran(kasId: String) {
        val buktiPembayaranRef = storage.reference.child("buktiPembayaranKas/$kasId.jpg")
        imageUri?.let { uri ->
            buktiPembayaranRef.putFile(uri)
                .addOnSuccessListener {
                    buktiPembayaranRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        db.collection("kas_mingguan").document(kasId)
                            .update(
                                "buktiPembayaranUrl", downloadUrl.toString(),
                                "status", "diterima"
                            )
                            .addOnSuccessListener {
                                Log.d(TAG, "Bukti pembayaran URL updated successfully.")
                                scheduleReminder()
                                sendNotificationToDkm(kasId)
                                Toast.makeText(this, "Kas berhasil dikirim", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error updating document")
                                Toast.makeText(this, "Gagal mengirim kas", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "Error updating document", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error uploading file", e)
                    Toast.makeText(this, "Gagal mengunggah bukti pembayaran", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Error uploading file", e)
                }
        }
    }

    private fun sendNotificationToDkm(kasId: String) {
        val notificationData = hashMapOf(
            "kasId" to kasId,
            "message" to "Kas mingguan telah diterima."
        )

        db.collection("notifikasi_dkm")
            .add(notificationData)
            .addOnSuccessListener {
                Log.d(TAG, "Notification sent to DKM")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error sending notification", e)
                Log.e(TAG, "Error sending notification", e)
            }
    }

    private fun scheduleReminder() {
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}