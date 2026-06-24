package com.dionisispx.expensetracker.domain.repository

// Repository interface for image processing operations
interface VisionRepository {
    // Extracts text from a given base64 encoded image
    suspend fun extractTextFromImage(base64Image: String): Result<String>
}
