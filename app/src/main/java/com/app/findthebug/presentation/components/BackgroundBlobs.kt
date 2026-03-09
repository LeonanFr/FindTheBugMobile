package com.app.findthebug.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun BackgroundBlobs() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawCircle(
            color = Color(0xFF9C27B0).copy(alpha = 0.15f),
            radius = width * 0.4f,
            center = Offset(x = width * 0.2f, y = height * 0.3f)
        )

        drawCircle(
            color = Color(0xFF7B1FA2).copy(alpha = 0.12f),
            radius = width * 0.5f,
            center = Offset(x = width * 0.8f, y = height * 0.7f)
        )

        drawCircle(
            color = Color(0xFFBA68C8).copy(alpha = 0.1f),
            radius = width * 0.3f,
            center = Offset(x = width * 0.5f, y = height * 0.5f)
        )
    }
}