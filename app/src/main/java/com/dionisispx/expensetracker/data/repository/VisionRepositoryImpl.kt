package com.dionisispx.expensetracker.data.repository

import android.util.Log
import com.dionisispx.expensetracker.BuildConfig
import com.dionisispx.expensetracker.data.remote.AnnotateImageRequest
import com.dionisispx.expensetracker.data.remote.Feature
import com.dionisispx.expensetracker.data.remote.VisionImage
import com.dionisispx.expensetracker.data.remote.VisionApi
import com.dionisispx.expensetracker.data.remote.VisionRequest
import com.dionisispx.expensetracker.domain.repository.VisionRepository
import javax.inject.Inject

class VisionRepositoryImpl @Inject constructor(
    private val visionApi: VisionApi
) : VisionRepository {

    override suspend fun extractTextFromImage(base64Image: String): Result<String> {
        return try {
            val request = VisionRequest(
                requests = listOf(
                    AnnotateImageRequest(
                        image = VisionImage(content = base64Image),
                        features = listOf(Feature(type = "DOCUMENT_TEXT_DETECTION"))
                    )
                )
            )

            val apiKey = BuildConfig.VISION_API_KEY

            Log.d("CloudVision", "Uploading image to Google Vision API...")
            val response = visionApi.annotateImage(apiKey, request)

            val extractedText = response.responses?.firstOrNull()?.textAnnotations?.firstOrNull()?.description ?: ""
            
            Log.d("CloudVision", "Found text: \n$extractedText")

            Result.success(extractedText)
        } catch (e: Exception) {
            Log.e("CloudVision", "Network or Parsing Error", e)
            Result.failure(e)
        }
    }
}
