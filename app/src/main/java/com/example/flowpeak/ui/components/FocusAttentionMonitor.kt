package com.example.flowpeak.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.example.flowpeak.analyzer.FaceAnalyzer

/** Monitor local, fără previzualizare: este activ numai în timpul unei sesiuni de focus. */
@Composable
fun FocusAttentionMonitor(
    enabled: Boolean,
    onAttentionChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { cameraGranted = it }

    LaunchedEffect(enabled, cameraGranted) {
        if (enabled && !cameraGranted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!enabled || !cameraGranted) return

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    DisposableEffect(cameraProviderFuture, lifecycleOwner) {
        val mainExecutor = ContextCompat.getMainExecutor(context)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(mainExecutor, FaceAnalyzer(onAttentionChanged)) }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, analysis)
            } catch (_: Exception) {
                onAttentionChanged(true)
            }
        }, mainExecutor)

        onDispose {
            if (cameraProviderFuture.isDone) cameraProviderFuture.get().unbindAll()
        }
    }
}
