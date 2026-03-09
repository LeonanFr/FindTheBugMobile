package com.app.findthebug.presentation.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.findthebug.presentation.components.BackButton
import com.app.findthebug.presentation.viewmodel.LobbyViewModel

@Composable
fun CreateLobbyScreen(
    viewModel: LobbyViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val session by viewModel.currentSession.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var playerName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F2429))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (session == null) "Criar sala" else "Sala criada",
                color = Color(0xFFE9EEF1),
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (session == null) {
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Seu nome", color = Color(0xFFBFC9D1)) },
                    singleLine = true,
                    modifier = Modifier.width(300.dp),
                    textStyle = TextStyle(color = Color(0xFFE9EEF1))
                )
            } else {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF151A1F), RoundedCornerShape(12.dp))
                        .padding(horizontal = 28.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = session!!.sessionId,
                        color = Color(0xFFF2F4F6),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(color = Color(0xFF00B7C3))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (session == null) {
                        viewModel.createLobby(playerName)
                    } else {
                        onContinue()
                    }
                },
                enabled = !isLoading && (session != null || playerName.isNotBlank()),
                modifier = Modifier
                    .width(230.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B7C3),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (session == null) "Criar sala" else "Continuar",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            BackButton(onClick = onBack)
        }
    }
}