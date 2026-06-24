package com.dionisispx.expensetracker.domain.util

// Interface for processing images
interface ImageProcessor {
    // Converts image from URI to Base64 string
    suspend fun getBase64FromUri(uriString: String): Result<String>
}
