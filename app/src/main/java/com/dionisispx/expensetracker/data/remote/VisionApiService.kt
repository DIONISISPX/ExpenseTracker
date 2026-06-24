package com.dionisispx.expensetracker.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// API service for Google Cloud Vision
interface VisionApi {
    // Annotates an image using the Vision API
    @POST("v1/images:annotate")
    suspend fun annotateImage(
        @Query("key") apiKey: String,
        @Body request: VisionRequest
    ): VisionResponse
}
