package com.app.findthebug.presentation.scenario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

data class DebugCase(
    val id: String,
    val title: String,
    val subtitle: String
)

@Composable
fun DebugScenarioScreen(
    cases: List<DebugCase>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit = {},
    onInvestigate: (DebugCase) -> Unit = {}
) {
    val background = Color(0xFF1F2429)
    val cardColor = Color(0xFF232A30)
    val cardStroke = Color(0xFF2D3941)
    val subtitleColor = Color(0xFF8A9095)
    val accent = Color(0xFF00B7C3)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp)
    ) {
        val scale = (this.maxWidth / 393.dp).coerceIn(0.85f, 1.2f)
        val cardWidth = 380.dp * scale
        val cardHeight = 218.dp * scale
        val buttonWidth = 321.dp * scale
        val buttonHeight = 52.dp * scale
        val titleSize = 25.sp * scale
        val subSize = 18.sp * scale
        val buttonTextSize = 20.sp * scale
        val gap = 16.dp * scale

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
                    text = "Cenário de Debug",
                    color = Color(0xFFE9EEF1),
                    fontSize = 28.sp * scale,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp * scale))

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp * scale
                )
                Spacer(modifier = Modifier.height(10.dp * scale))
            }

            if (isLoading) {
                Text(
                    text = "Carregando casos...",
                    color = Color(0xFFE9EEF1),
                    fontSize = 14.sp * scale
                )
                Spacer(modifier = Modifier.height(10.dp * scale))
            }

            val columns = if (this@BoxWithConstraints.maxWidth >= 720.dp) 2 else 1
            val rows = cases.chunked(columns)

            Column(
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                rows.forEach { rowCases ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(gap)
                    ) {
                        rowCases.forEach { item ->
                            DebugCaseCard(
                                case = item,
                                cardWidth = cardWidth,
                                cardHeight = cardHeight,
                                buttonWidth = buttonWidth,
                                buttonHeight = buttonHeight,
                                titleSize = titleSize,
                                subSize = subSize,
                                buttonTextSize = buttonTextSize,
                                cardColor = cardColor,
                                cardStroke = cardStroke,
                                subtitleColor = subtitleColor,
                                accent = accent,
                                onInvestigate = { onInvestigate(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugCaseCard(
    case: DebugCase,
    cardWidth: Dp,
    cardHeight: Dp,
    buttonWidth: Dp,
    buttonHeight: Dp,
    titleSize: androidx.compose.ui.unit.TextUnit,
    subSize: androidx.compose.ui.unit.TextUnit,
    buttonTextSize: androidx.compose.ui.unit.TextUnit,
    cardColor: Color,
    cardStroke: Color,
    subtitleColor: Color,
    accent: Color,
    onInvestigate: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .background(cardColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onInvestigate)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = case.title,
                    color = Color(0xFFE9EEF1),
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = case.subtitle,
                    color = subtitleColor,
                    fontSize = subSize
                )
            }
            Button(
                onClick = onInvestigate,
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Investigar",
                    fontSize = buttonTextSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
