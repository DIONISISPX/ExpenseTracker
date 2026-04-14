package com.dionisispx.expensetracker.presentation.add_expense

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun CameraPreview(
    imageCapture: ImageCapture,
    flashEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // State to hold CameraControl for focusing and torch
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraInfo by remember { mutableStateOf<CameraInfo?>(null) }

    // Maintain reference to the PreviewView instance for accurate autofocus metering
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // State for the focus indicator UI
    var focusPoint by remember { mutableStateOf<androidx.compose.ui.geometry.Offset?>(null) }
    val focusAlpha = remember { Animatable(0f) }

    // Unbind camera (which also explicitly turns off the torch) when the user leaves this composable/tab
    DisposableEffect(Unit) {
        onDispose {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Error unbinding camera on dispose", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    // Toggle the torch based on the passed state
    LaunchedEffect(flashEnabled, cameraControl) {
        if (cameraInfo?.hasFlashUnit() == true) {
            cameraControl?.enableTorch(flashEnabled)
        }
    }

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

                // Store the reference
                previewViewRef = previewView

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
                                previewViewRef?.let { view ->
                                    val factory = view.meteringPointFactory
                                    val point = factory.createPoint(offset.x, offset.y)
                                    val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                                        .build()

                                    control.startFocusAndMetering(action)
                                }
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