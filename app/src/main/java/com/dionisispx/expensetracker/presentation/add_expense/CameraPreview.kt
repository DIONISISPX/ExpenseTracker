package com.dionisispx.expensetracker.presentation.add_expense

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun CameraPreview(
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State to hold CameraControl for focusing
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraInfo by remember { mutableStateOf<CameraInfo?>(null) }

    // State for the focus indicator UI
    var focusPoint by remember { mutableStateOf<androidx.compose.ui.geometry.Offset?>(null) }
    val focusAlpha = remember { Animatable(0f) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = androidx.camera.core.Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                        cameraControl = camera.cameraControl
                        cameraInfo = camera.cameraInfo

                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            // Show the visual indicator
                            focusPoint = offset

                            // Perform camera focus
                            cameraControl?.let { control ->
                                // We assume the view fills the screen roughly.
                                // A more accurate implementation would map coordinates from the PreviewView,
                                // but SurfaceOrientedMeteringPointFactory works decently for full-screen Compose views.
                                val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                                    size.width.toFloat(),
                                    size.height.toFloat()
                                )
                                val point = factory.createPoint(offset.x, offset.y)
                                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                                    .build()

                                control.startFocusAndMetering(action)
                            }
                        }
                    )
                }
        )

        // The Visual Focus Indicator
        focusPoint?.let { point ->
            LaunchedEffect(point) {
                // Animate alpha to 1, wait a bit, then fade out
                focusAlpha.snapTo(1f)
                delay(800)
                focusAlpha.animateTo(0f, animationSpec = tween(500))
                // Clear the point so it can be re-triggered in the exact same spot
                if(focusAlpha.value == 0f) {
                    focusPoint = null
                }
            }

            Box(
                modifier = Modifier
                    .offset {
                        // Center the 60dp circle on the tap point
                        IntOffset(
                            x = point.x.toInt() - 30.dp.roundToPx(),
                            y = point.y.toInt() - 30.dp.roundToPx()
                        )
                    }
                    .size(60.dp)
                    .border(2.dp, Color.White.copy(alpha = focusAlpha.value), CircleShape)
            )
        }
    }
}