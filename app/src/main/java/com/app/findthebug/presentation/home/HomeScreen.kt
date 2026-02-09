package com.app.findthebug.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import kotlin.math.min

@Composable
fun HomeScreen(
    onPlay: () -> Unit = {},
    onTutorial: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val background = Color(0xFF1F2429)
    val accent = Color(0xFF00B7C3)
    val outline = Color(0xFF2E3A42)
    val red = Color(0xFFE02020)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 24.dp)
    ) {
        DecorativeBackground(
            modifier = Modifier.fillMaxSize(),
            outline = outline
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                BugIcon(color = red, size = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Find the ",
                    color = Color(0xFFE9EEF1),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Bug",
                    color = red,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onPlay,
                modifier = Modifier
                    .width(220.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Jogar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SecondaryButton(label = "Tutorial", onClick = onTutorial)
                SecondaryButton(label = "Configurações", onClick = onSettings)
            }
        }
    }
}

@Composable
private fun SecondaryButton(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .height(48.dp)
            .width(160.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF2D3941)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFF1C2126),
            contentColor = Color(0xFFF2F4F6)
        )
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DecorativeBackground(
    modifier: Modifier,
    outline: Color
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 2f)
        val minDim = min(size.width, size.height)

        drawCircle(
            color = outline.copy(alpha = 0.5f),
            radius = minDim * 0.28f,
            center = Offset(x = size.width * 0.12f, y = size.height * 0.32f),
            style = stroke
        )

        drawCircle(
            color = outline.copy(alpha = 0.4f),
            radius = minDim * 0.12f,
            center = Offset(x = size.width * 0.88f, y = size.height * 0.78f),
            style = stroke
        )

        val diamondSize = minDim * 0.18f
        val cx = size.width * 0.78f
        val cy = size.height * 0.48f
        val path = Path().apply {
            moveTo(cx, cy - diamondSize / 2f)
            lineTo(cx + diamondSize / 2f, cy)
            lineTo(cx, cy + diamondSize / 2f)
            lineTo(cx - diamondSize / 2f, cy)
            close()
        }
        drawPath(path, color = outline.copy(alpha = 0.35f), style = stroke)
    }
}

@Composable
private fun BugIcon(
    color: Color,
    size: Dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val minDim = min(this.size.width, this.size.height)
        val strokeWidth = minDim * 0.08f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val c = center
        val bodyRadius = minDim * 0.22f
        val headRadius = bodyRadius * 0.6f

        drawCircle(
            color = color,
            radius = headRadius,
            center = Offset(c.x, c.y - bodyRadius * 1.1f),
            style = stroke
        )

        drawCircle(
            color = color,
            radius = bodyRadius,
            center = c,
            style = stroke
        )

        drawLine(
            color = color,
            start = Offset(c.x, c.y - bodyRadius * 0.7f),
            end = Offset(c.x, c.y + bodyRadius * 0.7f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        val legOffset = bodyRadius * 0.9f
        val legLength = bodyRadius * 0.7f
        drawLine(
            color = color,
            start = Offset(c.x - legOffset, c.y - bodyRadius * 0.4f),
            end = Offset(c.x - legOffset - legLength, c.y - bodyRadius * 0.7f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(c.x - legOffset, c.y + bodyRadius * 0.1f),
            end = Offset(c.x - legOffset - legLength, c.y + bodyRadius * 0.4f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(c.x + legOffset, c.y - bodyRadius * 0.4f),
            end = Offset(c.x + legOffset + legLength, c.y - bodyRadius * 0.7f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(c.x + legOffset, c.y + bodyRadius * 0.1f),
            end = Offset(c.x + legOffset + legLength, c.y + bodyRadius * 0.4f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(c.x - headRadius * 0.6f, c.y - bodyRadius * 1.6f),
            end = Offset(c.x - headRadius * 1.2f, c.y - bodyRadius * 2.2f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(c.x + headRadius * 0.6f, c.y - bodyRadius * 1.6f),
            end = Offset(c.x + headRadius * 1.2f, c.y - bodyRadius * 2.2f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
