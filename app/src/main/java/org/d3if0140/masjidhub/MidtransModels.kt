package org.d3if0140.masjidhub

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("create-transaction")
    fun getMidtransToken(
        @Body transaction: ServerTransactionRequest
    ): Call<MidtransTokenResponse>
}

data class MidtransTokenResponse(
    val token: String
)

data class ServerTransactionRequest(
    val transactionId: String,
    val amount: Double
)

