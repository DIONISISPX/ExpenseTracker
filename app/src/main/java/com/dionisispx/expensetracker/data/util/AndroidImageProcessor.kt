package com.dionisispx.expensetracker.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.dionisispx.expensetracker.domain.util.ImageProcessor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class AndroidImageProcessor @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ImageProcessor {

    // Process image URI to base64 string
    override suspend fun getBase64FromUri(uriString: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val uri = uriString.toUri()
                var inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap != null) {
                    // Adjust image rotation based on metadata
                    inputStream = context.contentResolver.openInputStream(uri)
                    val exif = ExifInterface(inputStream!!)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    inputStream.close()

                    val matrix = Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    }

                    // Compress image dimensions to reduce payload size
                    val maxDimension = 1024f
                    val scale = minOf(maxDimension / originalBitmap.width, maxDimension / originalBitmap.height)

                    if (scale < 1f) {
                        matrix.postScale(scale, scale)
                    }
                    val processedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

                    // Free memory of original image
                    if (processedBitmap != originalBitmap) {
                        originalBitmap.recycle()
                    }

                    // Encode processed image to base64 string
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                    processedBitmap.recycle() // Free memory of compressed image

                    val imageBytes = byteArrayOutputStream.toByteArray()
                    val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                    Result.success(base64Image)
                } else {
                    Result.failure(Exception("Failed to decode image from URI"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
