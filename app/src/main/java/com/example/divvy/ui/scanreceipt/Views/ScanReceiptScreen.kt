package com.example.divvy.ui.scanreceipt.Views

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

private val Purple = Color(0xFF7C4DFF)
private val Blue = Color(0xFF448AFF)
private val YellowBadge = Color(0xFFFFC107)
private val GreenCheck = Color(0xFF4CAF50)

private enum class ScanState { Idle, Scanning, Done }

@Composable
fun ScanReceiptScreen(
    onBack: () -> Unit,
    onScanComplete: (amount: String, description: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var scanState by remember { mutableStateOf(ScanState.Idle) }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(scanState) {
        if (scanState == ScanState.Scanning) {
            delay(2500)
            scanState = ScanState.Done
        }
        if (scanState == ScanState.Done) {
            delay(800)
            onScanComplete("47.83", "Dinner at Maple Bistro")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview
                            )
                        } catch (_: Exception) { }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(onBack = onBack)

            Spacer(modifier = Modifier.weight(1f))

            ReceiptFrame()

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Align receipt within frame for best results",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            CaptureControls(
                isScanning = scanState != ScanState.Idle,
                onCapture = { if (scanState == ScanState.Idle) scanState = ScanState.Scanning }
            )

            Text(
                text = "Tap to capture receipt",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 32.dp)
            )
        }

        // Scanning overlay
        AnimatedVisibility(
            visible = scanState != ScanState.Idle,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            ScanningOverlay(scanState = scanState)
        }
    }
}

@Composable
private fun ScanningOverlay(scanState: ScanState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (scanState == ScanState.Scanning) {
                val infiniteTransition = rememberInfiniteTransition(label = "scan")
                val sweepAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "sweep"
                )

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(80.dp),
                        color = Purple,
                        strokeWidth = 4.dp
                    )
                    Canvas(modifier = Modifier.size(80.dp)) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(Color.Transparent, Blue)
                            ),
                            startAngle = sweepAngle,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Scanning receipt...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Detecting items and prices",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            } else if (scanState == ScanState.Done) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = GreenCheck,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Receipt scanned!",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "3 items detected  •  \$47.83",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(YellowBadge)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.FlashOn,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Auto-detect ON",
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ReceiptFrame() {
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerLen = 40.dp.toPx()
            val strokeW = 3.dp.toPx()
            val radius = 8.dp.toPx()
            val w = size.width
            val h = size.height

            // Top-left
            drawLine(Color.White, Offset(0f, radius), Offset(0f, cornerLen), strokeWidth = strokeW)
            drawArc(Color.White, 180f, 90f, false, Offset.Zero, Size(radius * 2, radius * 2), style = Stroke(strokeW, cap = StrokeCap.Round))
            drawLine(Color.White, Offset(radius, 0f), Offset(cornerLen, 0f), strokeWidth = strokeW)

            // Top-right
            drawLine(Color.White, Offset(w, radius), Offset(w, cornerLen), strokeWidth = strokeW)
            drawArc(Color.White, 270f, 90f, false, Offset(w - radius * 2, 0f), Size(radius * 2, radius * 2), style = Stroke(strokeW, cap = StrokeCap.Round))
            drawLine(Color.White, Offset(w - radius, 0f), Offset(w - cornerLen, 0f), strokeWidth = strokeW)

            // Bottom-left
            drawLine(Color.White, Offset(0f, h - radius), Offset(0f, h - cornerLen), strokeWidth = strokeW)
            drawArc(Color.White, 90f, 90f, false, Offset(0f, h - radius * 2), Size(radius * 2, radius * 2), style = Stroke(strokeW, cap = StrokeCap.Round))
            drawLine(Color.White, Offset(radius, h), Offset(cornerLen, h), strokeWidth = strokeW)

            // Bottom-right
            drawLine(Color.White, Offset(w, h - radius), Offset(w, h - cornerLen), strokeWidth = strokeW)
            drawArc(Color.White, 0f, 90f, false, Offset(w - radius * 2, h - radius * 2), Size(radius * 2, radius * 2), style = Stroke(strokeW, cap = StrokeCap.Round))
            drawLine(Color.White, Offset(w - radius, h), Offset(w - cornerLen, h), strokeWidth = strokeW)
        }

        Text(
            text = "Position receipt\nhere",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun CaptureControls(
    isScanning: Boolean,
    onCapture: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(enabled = !isScanning) { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = "Gallery",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Purple.copy(alpha = 0.3f))
                .clickable(enabled = !isScanning, onClick = onCapture),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Purple)
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(enabled = !isScanning) { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FlashOn,
                contentDescription = "Flash",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
