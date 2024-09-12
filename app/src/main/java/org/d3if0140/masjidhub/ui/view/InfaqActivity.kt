package org.d3if0140.masjidhub.ui.view

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import org.d3if0140.masjidhub.data.api.ApiService
import org.d3if0140.masjidhub.databinding.ActivityInfaqBinding
import org.d3if0140.masjidhub.model.MidtransTokenResponse
import org.d3if0140.masjidhub.model.ServerTransactionRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class InfaqActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfaqBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var transactionId: String? = null
    private val TAG = "InfaqActivity"

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
                            sendNotification(id, jumlah = 0.0)
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
        binding = ActivityInfaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengatur tanggal saat ini ke EditText tanggal
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        binding.editTextTanggal.setText(currentDate)

        binding.editTextTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.buttonBayar.setOnClickListener {
            submitInfaq()
        }
    }

    private fun submitInfaq() {
        val jumlahString = binding.editTextJumlahInfaq.text.toString()
        val jumlah = jumlahString.toDoubleOrNull() ?: 0.0

        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: ""
        val email = currentUser?.email ?: "Unknown"
        transactionId = UUID.randomUUID().toString()
        val tanggal = binding.editTextTanggal.text.toString()

        val infaqData = hashMapOf(
            "userId" to userId,
            "jumlah" to jumlah, // Pastikan ini bertipe Double
            "email" to email,
            "status" to "pending",
            "tanggal" to tanggal,
            "tipe" to "infaq",
            "metodePembayaran" to "unknown" // Menyimpan metode pembayaran sementara
        )

        transactionId?.let { id ->
            db.collection("transaksi_keuangan").document(id)
                .set(infaqData)
                .addOnSuccessListener {
                    Log.d(TAG, "Document successfully written!")
                    sendNotification(id, jumlah) // Mengirim jumlah sebagai Double
                    getTokenFromServer(id, jumlah)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                    Toast.makeText(this, "Gagal mengirim infaq", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getTokenFromServer(transactionId: String, amount: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://molly-hot-lioness.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val transactionRequest = ServerTransactionRequest(transactionId, amount)

        apiService.getMidtransToken(transactionRequest).enqueue(object :
            Callback<MidtransTokenResponse> {
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

    private fun sendNotification(transactionId: String, jumlah: Double) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "unknown_user"

        // Format jumlah tanpa desimal
        val formattedJumlah = jumlah.toInt().toString()

        val notificationData = hashMapOf(
            "userId" to userId,
            "title" to "Infaq sedang diproses",
            "message" to "Infaq sebesar Rp $formattedJumlah telah diterima oleh aplikasi.", // $jumlah harus bertipe Double
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("notifikasi")
            .add(notificationData)
            .addOnSuccessListener {
                // Notifikasi berhasil disimpan
            }
            .addOnFailureListener { e ->
                // Gagal menyimpan notifikasi
                e.printStackTrace()
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
