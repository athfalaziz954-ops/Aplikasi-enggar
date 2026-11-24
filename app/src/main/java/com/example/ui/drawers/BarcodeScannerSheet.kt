package com.example.ui.drawers

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerSheet(
    onBarcodeScanned: (String) -> Unit,
    onSimulateRandomScan: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var manualBarcode by remember { mutableStateOf("") }
    var showScanFlash by remember { mutableStateOf(false) }
    var lastDetectedTimestamp by remember { mutableLongStateOf(0L) }

    LaunchedEffect(showScanFlash) {
        if (showScanFlash) {
            delay(320L)
            showScanFlash = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "laser_anim")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PearlWhite.copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.QrCodeScanner,
                        contentDescription = null,
                        tint = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pemindai Barcode",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = Slate700)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Camera Viewfinder Box with Animated Laser Line & Flash
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Slate900)
                    .border(2.dp, EmeraldPrimary.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    CameraXBarcodePreview(
                        onCodeDetected = { code ->
                            val now = System.currentTimeMillis()
                            if (now - lastDetectedTimestamp > 1400L) {
                                lastDetectedTimestamp = now
                                showScanFlash = true
                                onBarcodeScanned(code)
                            }
                        }
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Izin kamera diperlukan untuk memindai barcode secara langsung.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                        ) {
                            Text("Izinkan Kamera", color = Color.White)
                        }
                    }
                }

                // Laser guide box & animated emerald laser line
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.76f)
                        .height(120.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (112 * laserProgress).dp)
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, EmeraldVibrant, Color.Transparent)
                                )
                            )
                    )
                }

                // Visual Scan Flash
                androidx.compose.animation.AnimatedVisibility(
                    visible = showScanFlash,
                    enter = fadeIn(tween(80)),
                    exit = fadeOut(tween(240)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(EmeraldVibrant.copy(alpha = 0.45f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Manual Barcode Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = { manualBarcode = it },
                    label = { Text("Input Kode Barcode Manual") },
                    placeholder = { Text("Contoh: 8991002100011") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("manual_barcode_input")
                )
                Button(
                    onClick = {
                        if (manualBarcode.isNotBlank()) {
                            showScanFlash = true
                            onBarcodeScanned(manualBarcode.trim())
                            manualBarcode = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(54.dp)
                ) {
                    Text("Cari", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Test Simulation Button
            OutlinedButton(
                onClick = {
                    showScanFlash = true
                    onSimulateRandomScan()
                },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("simulate_scan_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Casino,
                    contentDescription = null,
                    tint = EmeraldDeep,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Simulasi Scan Acak",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldDeep
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tips: Kode contoh tersedia pada produk awal di tab Stok.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraXBarcodePreview(
    onCodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { imageAnalysis ->
                        imageAnalysis.setAnalyzer(executor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        val firstRaw = barcodes.firstOrNull()?.rawValue
                                        if (!firstRaw.isNullOrBlank()) {
                                            onCodeDetected(firstRaw)
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                Log.w("BarcodeScanner", "Camera preview unavailable: ${e.localizedMessage}")
            }
        }
        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (_: Exception) {
            }
            scanner.close()
            executor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}
