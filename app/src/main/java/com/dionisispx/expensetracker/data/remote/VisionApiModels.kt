package com.dionisispx.expensetracker.data.remote

import com.google.gson.annotations.SerializedName

// Request Models
data class VisionRequest(val requests: List<AnnotateImageRequest>)
data class AnnotateImageRequest(val image: VisionImage, val features: List<Feature>)
data class VisionImage(val content: String) // Base64 Image
data class Feature(val type: String = "TEXT_DETECTION")

// Response Models
data class VisionResponse(val responses: List<AnnotateImageResponse>?)
data class AnnotateImageResponse(val textAnnotations: List<TextAnnotation>?)
data class TextAnnotation(
    @SerializedName("description") val description: String
)