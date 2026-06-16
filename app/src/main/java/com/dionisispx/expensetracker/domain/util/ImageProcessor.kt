package com.dionisispx.expensetracker.domain.util

interface ImageProcessor {
    suspend fun getBase64FromUri(uriString: String): Result<String>
}
