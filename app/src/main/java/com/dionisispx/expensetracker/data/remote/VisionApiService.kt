package com.dionisispx.expensetracker.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface VisionApi {
    @POST("v1/images:annotate")
    suspend fun annotateImage(
        @Query("key") apiKey: String,
        @Body request: VisionRequest
    ): VisionResponse
}
