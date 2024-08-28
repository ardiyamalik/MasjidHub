package org.d3if0140.masjidhub

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import org.d3if0140.masjidhub.databinding.ActivityPengisianKasBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class PengisianKasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPengisianKasBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null
    private val TAG = "PengisianKasActivity"

    // Variabel anggota untuk menyimpan kasId
    private var kasId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi SDK Midtrans
        SdkUIFlowBuilder.init()
            .setClientKey("SB-Mid-client-4dgiBpGWaRHQR25r")
            .setContext(this)
            .setTransactionFinishedCallback { result ->
                // Cek hasil transaksi
                if (result.status == TransactionResult.STATUS_SUCCESS) {
                    kasId?.let {
                        sendNotificationToDkm(it)
                        startActivity(Intent(this, KeuanganDkmActivity::class.java))
                    }
                } else if (result.status == TransactionResult.STATUS_PENDING) {
                    Toast.makeText(this, "Pembayaran pending, silakan cek nanti.", Toast.LENGTH_SHORT).show()
                } else if (result.status == TransactionResult.STATUS_FAILED) {
                    Toast.makeText(this, "Pembayaran gagal.", Toast.LENGTH_SHORT).show()
                }
            }
            .setMerchantBaseUrl("https://c6d2-202-46-68-250.ngrok-free.app")
            .enableLog(true) // Untuk debugging, ganti ke false untuk production
            .buildSDK()


        binding = ActivityPengisianKasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        binding.editTextTanggal.setText(currentDate)

        binding.editTextTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, DkmDashboard::class.java))
        }

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
                binding.textViewRekening.visibility = View.VISIBLE
                binding.imageViewQR.visibility = View.GONE
            } else {
                binding.textViewRekening.visibility = View.GONE
                binding.imageViewQR.visibility = View.VISIBLE
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

        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: ""
        val email = currentUser?.email ?: "Unknown"
        val kasId = UUID.randomUUID().toString()
        val tanggal = binding.editTextTanggal.text.toString()

        val kasData = hashMapOf(
            "userId" to userId,
            "jumlah" to jumlah,
            "metode" to metode,
            "email" to email,
            "status" to "pending",
            "tanggal" to tanggal,
            "buktiPembayaranUrl" to "" // Tambahkan ini untuk bukti pembayaran URL
        )

        db.collection("kas_mingguan").document(kasId)
            .set(kasData)
            .addOnSuccessListener {
                Log.d(TAG, "Document successfully written!")
                uploadBuktiPembayaran(kasId)
                sendNotificationToDkm(kasId)
                getTokenFromServer(kasId, jumlah)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(this, "Gagal mengirim kas", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getTokenFromServer(kasId: String, amount: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://c6d2-202-46-68-250.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val transactionRequest = ServerTransactionRequest(kasId, amount)

        apiService.getMidtransToken(transactionRequest).enqueue(object : Callback<MidtransTokenResponse> {
            override fun onResponse(call: Call<MidtransTokenResponse>, response: Response<MidtransTokenResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        startMidtransPayment(token)
                    } else {
                        Log.e(TAG, "Token tidak tersedia")
                    }
                } else {
                    Log.e(TAG, "Gagal mendapatkan token. Response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<MidtransTokenResponse>, t: Throwable) {
                Log.e(TAG, "Error requesting token", t)
            }
        })
    }

    private fun startMidtransPayment(token: String) {
        Log.d(TAG, "Starting payment with token: $token")
        MidtransSDK.getInstance().startPaymentUiFlow(this, token)
    }




    private fun uploadBuktiPembayaran(kasId: String) {
        val buktiPembayaranRef = storage.reference.child("buktiPembayaranKas").child("$kasId.jpg")
        imageUri?.let { uri ->
            buktiPembayaranRef.putFile(uri)
                .addOnSuccessListener { _ ->
                    buktiPembayaranRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        db.collection("kas_mingguan").document(kasId)
                            .update(
                                "buktiPembayaranUrl", downloadUrl.toString(),
                                "status", "pending"
                            )
                            .addOnSuccessListener {
                                Log.d(TAG, "Bukti pembayaran URL updated successfully.")
                                scheduleReminder()
                                sendNotificationToDkm(kasId)
                                Toast.makeText(this, "Kas berhasil dikirim", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error updating document", e)
                                Toast.makeText(this, "Gagal mengirim kas", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error uploading file", e)
                    Toast.makeText(this, "Gagal mengunggah bukti pembayaran", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun sendNotificationToDkm(kasId: String) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "unknown_user"

        val notificationData = hashMapOf(
            "userId" to userId,
            "title" to "kas sedang diproses",
            "message" to "Kas mingguan sedang diproses oleh aplikasi.",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("notifikasi_pengurus_dkm")
            .add(notificationData)
            .addOnSuccessListener {
                Log.d(TAG, "Notification sent to DKM")
                // Hanya mengirim notifikasi, tidak berpindah halaman
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error sending notification", e)
                Toast.makeText(this, "Gagal mengirim notifikasi", Toast.LENGTH_SHORT).show()
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

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.editTextTanggal.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}

interface ApiService {
    @POST("create-transaction")
    fun getMidtransToken(
        @Body transaction: ServerTransactionRequest
    ): Call<MidtransTokenResponse>
}

data class MidtransTokenResponse(val token: String)

data class ServerTransactionRequest(
    val kasId: String,
    val amount: Double
)

