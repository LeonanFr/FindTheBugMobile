package com.app.findthebug.presentation.case

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CaseSection(
    val title: String
)

@Composable
fun CaseDetailScreen(
    title: String,
    scorePoints: Int,
    daysLeft: Int,
    sections: List<CaseSection>,
    onBack: () -> Unit,
    onDeclareSolution: () -> Unit
) {
    val background = Color(0xFF1F2429)
    val cardColor = Color(0xFF232A30)
    val cardStroke = Color(0xFF2D3941)
    val accent = Color(0xFF00B7C3)
    val danger = Color(0xFFDE1B1B)
    val muted = Color(0xFF8A9095)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp)
    ) {
        val scale = (this.maxWidth / 393.dp).coerceIn(0.85f, 1.2f)
        val cardWidth = 266.dp * scale
        val cardHeight = 110.dp * scale
        val gap = 14.dp * scale
        val cardTextSize = 14.sp * scale

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp * scale)
                        .background(Color(0xFF1C2126), RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = "<",
                        color = Color(0xFFE9EEF1),
                        fontSize = 18.sp * scale
                    )
                }
                Spacer(modifier = Modifier.width(12.dp * scale))
                Text(
                    text = title,
                    color = Color(0xFFE9EEF1),
                    fontSize = 22.sp * scale,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onDeclareSolution,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = danger,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Declarar Solução",
                        fontSize = 12.sp * scale,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp * scale))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp * scale)
            ) {
                Text(
                    text = "Pontos de Função: $scorePoints",
                    color = accent,
                    fontSize = 12.sp * scale,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Dias Restantes: $daysLeft",
                    color = Color(0xFFE9EEF1),
                    fontSize = 12.sp * scale,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp * scale))

            val columns = if (this@BoxWithConstraints.maxWidth >= 720.dp) 2 else 1
            val rows = sections.chunked(columns)

            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(gap)
                ) {
                    rows.forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                            rowItems.forEach { item ->
                                SectionCard(
                                    title = item.title,
                                cardWidth = cardWidth,
                                cardHeight = cardHeight,
                                textSize = cardTextSize,
                                cardColor = cardColor,
                                cardStroke = cardStroke
                            )
                        }
                    }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp * scale))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp * scale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(40.dp * scale)
                            .background(Color(0xFF1C2126), RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = "DOC",
                            color = Color(0xFFE9EEF1),
                            fontSize = 16.sp * scale
                        )
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(40.dp * scale)
                            .background(Color(0xFF1C2126), RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = "i",
                            color = Color(0xFFE9EEF1),
                            fontSize = 16.sp * scale,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    cardWidth: Dp,
    cardHeight: Dp,
    textSize: androidx.compose.ui.unit.TextUnit,
    cardColor: Color,
    cardStroke: Color
) {
    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .background(cardColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color(0xFFE9EEF1),
                fontSize = textSize,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "",
                color = Color.Transparent
            )
        }
    }
}
