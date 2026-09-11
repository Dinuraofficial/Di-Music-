package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun DiMusicLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val w = size.width
        val h = size.height
        
        // Black rounded square background
        drawRoundRect(
            color = Color(0xFF111111),
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.22f, h * 0.22f)
        )
        
        val redWaveColor1 = Color(0xFFE91E63).copy(alpha = 0.85f)
        val redWaveColor2 = Color(0xFFFF1744).copy(alpha = 0.9f)
        val redWaveColor3 = Color(0xFFD50000).copy(alpha = 0.7f)

        // Waveform 1 (Back)
        val path1 = Path().apply {
            moveTo(w * 0.1f, h * 0.5f)
            cubicTo(w * 0.25f, h * 0.1f, w * 0.4f, h * 0.9f, w * 0.5f, h * 0.5f)
            cubicTo(w * 0.6f, h * 0.1f, w * 0.75f, h * 0.9f, w * 0.9f, h * 0.5f)
            lineTo(w * 0.9f, h * 0.6f)
            cubicTo(w * 0.75f, h * 1.0f, w * 0.6f, h * 0.2f, w * 0.5f, h * 0.6f)
            cubicTo(w * 0.4f, h * 1.0f, w * 0.25f, h * 0.2f, w * 0.1f, h * 0.6f)
            close()
        }
        drawPath(path = path1, color = redWaveColor3)

        // Waveform 2 (Middle)
        val path2 = Path().apply {
            moveTo(w * 0.1f, h * 0.5f)
            cubicTo(w * 0.3f, h * 0.2f, w * 0.4f, h * 0.8f, w * 0.5f, h * 0.5f)
            cubicTo(w * 0.6f, h * 0.2f, w * 0.7f, h * 0.8f, w * 0.9f, h * 0.5f)
            lineTo(w * 0.9f, h * 0.55f)
            cubicTo(w * 0.7f, h * 0.85f, w * 0.6f, h * 0.25f, w * 0.5f, h * 0.55f)
            cubicTo(w * 0.4f, h * 0.85f, w * 0.3f, h * 0.25f, w * 0.1f, h * 0.55f)
            close()
        }
        drawPath(path = path2, color = redWaveColor1)

        // Waveform 3 (Front, sharpest)
        val path3 = Path().apply {
            moveTo(w * 0.1f, h * 0.5f)
            cubicTo(w * 0.25f, h * 0.3f, w * 0.4f, h * 0.7f, w * 0.5f, h * 0.5f)
            cubicTo(w * 0.6f, h * 0.2f, w * 0.7f, h * 0.6f, w * 0.9f, h * 0.5f)
            lineTo(w * 0.9f, h * 0.52f)
            cubicTo(w * 0.7f, h * 0.62f, w * 0.6f, h * 0.22f, w * 0.5f, h * 0.52f)
            cubicTo(w * 0.4f, h * 0.72f, w * 0.25f, h * 0.32f, w * 0.1f, h * 0.52f)
            close()
        }
        drawPath(path = path3, color = redWaveColor2)
    }
}
