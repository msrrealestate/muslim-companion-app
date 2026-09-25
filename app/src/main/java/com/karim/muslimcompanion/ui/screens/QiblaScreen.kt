package com.karim.muslimcompanion.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.karim.muslimcompanion.R
import com.karim.muslimcompanion.viewmodel.QiblaUiState
import com.karim.muslimcompanion.viewmodel.QiblaViewModel
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    viewModel: QiblaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        viewModel.startListening()
        onDispose { viewModel.stopListening() }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.qibla_title), fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                is QiblaUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
                is QiblaUiState.Error -> {
                    val message = if (state.message == "no_sensor") {
                        stringResource(id = R.string.qibla_sensor_missing)
                    } else {
                        stringResource(id = R.string.location_error)
                    }
                    Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
                is QiblaUiState.Ready -> {
                    Text(
                        text = stringResource(id = R.string.qibla_instructions),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    CompassDial(rotationDegrees = state.state.arrowRotation)

                    Text(
                        text = "${stringResource(id = R.string.qibla_degree_label)}: ${state.state.bearingToMecca.toInt()}°",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 24.dp)
                    )

                    Text(
                        text = stringResource(id = R.string.qibla_calibrate),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompassDial(rotationDegrees: Float) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = min(size.width, size.height) / 2f * 0.85f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer dial ring.
            drawCircle(
                color = surfaceColor,
                radius = radius,
                center = center,
                style = Stroke(width = 6.dp.toPx())
            )

            // Cardinal tick marks every 30 degrees.
            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30).toDouble())
                val outer = Offset(
                    x = center.x + (radius * sin(angle)).toFloat(),
                    y = center.y - (radius * cos(angle)).toFloat()
                )
                val inner = Offset(
                    x = center.x + ((radius - 14.dp.toPx()) * sin(angle)).toFloat(),
                    y = center.y - ((radius - 14.dp.toPx()) * cos(angle)).toFloat()
                )
                drawLine(color = surfaceColor, start = inner, end = outer, strokeWidth = 3.dp.toPx())
            }

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = textColor.toArgbCompat()
                    textSize = 14.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText("N", center.x, center.y - radius + 26.dp.toPx(), paint)
                drawText("S", center.x, center.y + radius - 12.dp.toPx(), paint)
                drawText("E", center.x + radius - 16.dp.toPx(), center.y + 6.dp.toPx(), paint)
                drawText("W", center.x - radius + 16.dp.toPx(), center.y + 6.dp.toPx(), paint)
            }

            // Qibla arrow, rotated toward Mecca relative to the device heading.
            rotate(degrees = rotationDegrees, pivot = center) {
                val arrowLength = radius * 0.78f
                val tip = Offset(center.x, center.y - arrowLength)
                val tailLeft = Offset(center.x - 14.dp.toPx(), center.y + 10.dp.toPx())
                val tailRight = Offset(center.x + 14.dp.toPx(), center.y + 10.dp.toPx())

                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(tip.x, tip.y)
                    lineTo(tailLeft.x, tailLeft.y)
                    lineTo(center.x, center.y)
                    lineTo(tailRight.x, tailRight.y)
                    close()
                }
                drawPath(path = path, color = goldColor)
                drawCircle(color = primaryColor, radius = 10.dp.toPx(), center = center)
            }
        }
    }
}

private fun Color.toArgbCompat(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
