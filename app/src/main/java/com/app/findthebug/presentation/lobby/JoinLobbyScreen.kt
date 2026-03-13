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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@Composable
fun JoinLobbyScreen(
    viewModel: LobbyViewModel,
    onJoinSuccess: (String) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var playerName by remember { mutableStateOf("") }
    var lobbyCode by remember { mutableStateOf("") }

    LaunchedEffect(uiState.session) {
        uiState.session?.let {
            onJoinSuccess(it.sessionId)
        }
    }

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
                text = "Entrar na sala",
                color = Color(0xFFE9EEF1),
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Seu nome", color = Color(0xFFBFC9D1)) },
                singleLine = true,
                modifier = Modifier.width(300.dp),
                textStyle = TextStyle(color = Color(0xFFE9EEF1))
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = lobbyCode,
                onValueChange = { lobbyCode = it },
                label = { Text("Código da sala", color = Color(0xFFBFC9D1)) },
                placeholder = { Text("Ex: FTB-9X42", color = Color(0xFF8A97A1)) },
                singleLine = true,
                modifier = Modifier.width(300.dp),
                textStyle = TextStyle(color = Color(0xFFE9EEF1))
            )

            if (!uiState.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(color = Color(0xFF00B7C3))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        viewModel.joinLobby(lobbyCode, playerName)
                    }
                },
                enabled = !uiState.isLoading && playerName.isNotBlank() && lobbyCode.isNotBlank(),
                modifier = Modifier
                    .width(230.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B7C3),
                    contentColor = Color.White
                )
            ) {
                Text("Entrar", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
            BackButton(onClick = onBack)
        }
    }
}