package com.app.findthebug.presentation.victory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VictoryScreen(
    onBackToHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F2429)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "VITÓRIA!",
                color = Color(0xFF00B7C3),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "A equipe identificou corretamente a falha.",
                color = Color(0xFFE9EEF1),
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onBackToHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B7C3),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp)
            ) {
                Text("Voltar ao Início", fontWeight = FontWeight.Bold)
            }
        }
    }
}