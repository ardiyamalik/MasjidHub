package org.d3if0140.masjidhub

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import org.d3if0140.masjidhub.databinding.ActivityPengisianKasBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PengisianKasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPengisianKasBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "PengisianKasActivity"

    // Variabel anggota untuk menyimpan transactionId
    private var transactionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi SDK Midtrans
        SdkUIFlowBuilder.init()
            .setClientKey("SB-Mid-client-4dgiBpGWaRHQR25r")
            .setContext(this)
            .setTransactionFinishedCallback { result ->
                Log.d(TAG, "Transaction result callback triggered")
                Log.d(TAG, "Transaction result: ${result.response?.toString()}") // Log respons lengkap

                transactionId?.let { id ->
                    val status = result.status
                    // Gunakan hasil log untuk memeriksa struktur dan data yang tersedia
                    val paymentType = result.response?.toString() ?: "unknown" // Menyimpan metode pembayaran sebagai string default

                    when (status) {
                        TransactionResult.STATUS_SUCCESS -> {
                            Log.d(TAG, "Transaction successful, updating status...")
                            updateTransactionStatus(id, "approved", paymentType)
                            sendNotificationToDkm(id)
                            finish() // Menutup activity setelah transaksi berhasil
                        }
                        TransactionResult.STATUS_PENDING -> {
                            Log.d(TAG, "Transaction pending")
                            Toast.makeText(this, "Pembayaran pending, silakan cek nanti.", Toast.LENGTH_SHORT).show()
                        }
                        TransactionResult.STATUS_FAILED -> {
                            Log.d(TAG, "Transaction failed")
                            updateTransactionStatus(id, "failed", paymentType)
                            Toast.makeText(this, "Pembayaran gagal.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
            .setMerchantBaseUrl("https://molly-hot-lioness.ngrok-free.app")
            .enableLog(true) // Untuk debugging, ganti ke false untuk production
            .buildSDK()
        Log.d(TAG, "Midtrans SDK initialized")


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

        binding.buttonBayar.setOnClickListener {
            submitKas()
        }
    }

    private fun submitKas() {
        val jumlah = 50000.0 // Jumlah kas tetap

        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: ""
        val email = currentUser?.email ?: "Unknown"
        val transactionId = UUID.randomUUID().toString()
        val tanggal = binding.editTextTanggal.text.toString()

        val kasData = hashMapOf(
            "userId" to userId,
            "jumlah" to jumlah,
            "email" to email,
            "status" to "pending",
            "tanggal" to tanggal,
            "tipe" to "kas"
        )

        db.collection("transaksi_keuangan").document(transactionId)
            .set(kasData)
            .addOnSuccessListener {
                Log.d(TAG, "Document successfully written!")
                sendNotificationToDkm(transactionId)
                getTokenFromServer(transactionId, jumlah)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(this, "Gagal mengirim kas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getTokenFromServer(transactionId: String, amount: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://molly-hot-lioness.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val transactionRequest = ServerTransactionRequest(transactionId, amount)

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

    private fun updateTransactionStatus(transactionId: String, status: String, paymentType: String) {
        db.collection("transaksi_keuangan").document(transactionId)
            .update("status", status, "metodePembayaran", paymentType)
            .addOnSuccessListener {
                Log.d(TAG, "Transaction status updated to $status with paymentType $paymentType")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating transaction status", e)
            }
    }


    private fun startMidtransPayment(token: String) {
        Log.d(TAG, "Starting payment with token: $token")
        MidtransSDK.getInstance().startPaymentUiFlow(this, token)
    }

    private fun updateTransactionStatus(transactionId: String, status: String) {
        Log.d(TAG, "Updating transaction status: transactionId=$transactionId, status=$status")
        db.collection("transaksi_keuangan").document(transactionId)
            .update("status", status)
            .addOnSuccessListener {
                Log.d(TAG, "Transaction status updated to $status")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating transaction status", e)
            }
    }




    private fun sendNotificationToDkm(transactionId: String) {
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

