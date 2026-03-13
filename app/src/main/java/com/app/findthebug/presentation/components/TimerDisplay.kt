package com.app.findthebug.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TimerDisplay(
    initialSeconds: Int,
    isActive: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secondsLeft by remember { mutableIntStateOf(initialSeconds) }

    LaunchedEffect(isActive, initialSeconds) {
        secondsLeft = initialSeconds
        if (isActive) {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
            onFinish()
        }
    }

    if (isActive) {
        Box(
            modifier = modifier
                .background(Color(0xFF2E3A44), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$secondsLeft s",
                color = Color(0xFFFFA500),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}