package com.app.findthebug.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DayCounter(daysLeft: Int, modifier: Modifier = Modifier) {
    val color = if (daysLeft > 0) Color(0xFFE9EEF1) else Color(0xFFFF6B6B)
    Box(
        modifier = modifier
            .background(Color(0xFF2E3A44), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Dias: $daysLeft",
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}