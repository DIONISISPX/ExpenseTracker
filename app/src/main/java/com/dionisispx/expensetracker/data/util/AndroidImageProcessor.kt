package com.dionisispx.expensetracker.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import com.dionisispx.expensetracker.domain.util.ImageProcessor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class AndroidImageProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageProcessor {

    override suspend fun getBase64FromUri(uriString: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                var inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap != null) {
                    // Correct orientation
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

                    // Scale image down
                    val maxDimension = 1024f
                    val scale = minOf(maxDimension / originalBitmap.width, maxDimension / originalBitmap.height)

                    if (scale < 1f) {
                        matrix.postScale(scale, scale)
                    }
                    val processedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

                    // Clean up original bitmap if it's different from the processed one
                    if (processedBitmap != originalBitmap) {
                        originalBitmap.recycle()
                    }

                    // Convert to base64
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                    processedBitmap.recycle() // Clean up after compression

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
