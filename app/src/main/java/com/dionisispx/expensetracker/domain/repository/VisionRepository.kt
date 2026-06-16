package com.dionisispx.expensetracker.domain.repository

interface VisionRepository {
    suspend fun extractTextFromImage(base64Image: String): Result<String>
}
