package org.d3if0140.masjidhub.data.api

import org.d3if0140.masjidhub.model.MidtransTokenResponse
import org.d3if0140.masjidhub.model.ServerTransactionRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("create-transaction")
    fun getMidtransToken(
        @Body transaction: ServerTransactionRequest
    ): Call<MidtransTokenResponse>
}


