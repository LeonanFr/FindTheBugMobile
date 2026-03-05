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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayLobbyScreen(
    onCreateLobby: () -> Unit,
    onJoinLobby: () -> Unit,
    onBack: () -> Unit
) {
    val background = Color(0xFF1F2429)
    val accent = Color(0xFF00B7C3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Lobby",
                color = Color(0xFFE9EEF1),
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onCreateLobby,
                modifier = Modifier
                    .width(280.dp)
                    .height(58.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Criar nova sala", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onJoinLobby,
                modifier = Modifier
                    .width(280.dp)
                    .height(58.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF1C2126),
                    contentColor = Color(0xFFF2F4F6)
                )
            ) {
                Text(text = "Entrar em uma sala", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Voltar",
                color = Color(0xFF92A0AA),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 6.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.width(160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF1C2126),
                    contentColor = Color(0xFFF2F4F6)
                )
            ) {
                Text(text = "Home")
            }
        }
    }
}

@Composable
fun CreateLobbyScreen(
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    lobbyCode: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onCreateLobby: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val background = Color(0xFF1F2429)
    val accent = Color(0xFF00B7C3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (lobbyCode == null) "Criar sala" else "Sala criada",
                color = Color(0xFFE9EEF1),
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (lobbyCode == null) {
                OutlinedTextField(
                    value = playerName,
                    onValueChange = onPlayerNameChange,
                    label = { Text(text = "Seu nome", color = Color(0xFFBFC9D1)) },
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
                        text = lobbyCode,
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
                    text = errorMessage,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Carregando...",
                    color = Color(0xFFE9EEF1),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (lobbyCode == null) onCreateLobby() else onContinue()
                },
                enabled = !isLoading && (if (lobbyCode == null) playerName.isNotBlank() else true),
                modifier = Modifier
                    .width(230.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (lobbyCode == null) "Criar sala" else "Continuar",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.width(160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF1C2126),
                    contentColor = Color(0xFFF2F4F6)
                )
            ) {
                Text(text = "Voltar")
            }
        }
    }
}

@Composable
fun JoinLobbyScreen(
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    lobbyCode: String,
    onLobbyCodeChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onJoin: () -> Unit,
    onBack: () -> Unit
) {
    val background = Color(0xFF1F2429)
    val accent = Color(0xFF00B7C3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
                onValueChange = onPlayerNameChange,
                label = { Text(text = "Seu nome", color = Color(0xFFBFC9D1)) },
                singleLine = true,
                modifier = Modifier.width(300.dp),
                textStyle = TextStyle(color = Color(0xFFE9EEF1))
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = lobbyCode,
                onValueChange = onLobbyCodeChange,
                label = { Text(text = "Codigo da sala", color = Color(0xFFBFC9D1)) },
                placeholder = { Text(text = "Ex: FTB-9X42", color = Color(0xFF8A97A1)) },
                singleLine = true,
                modifier = Modifier.width(300.dp),
                textStyle = TextStyle(color = Color(0xFFE9EEF1))
            )
            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
            if (isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Entrando na sala...",
                    color = Color(0xFFE9EEF1),
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onJoin,
                enabled = !isLoading && playerName.isNotBlank() && lobbyCode.isNotBlank(),
                modifier = Modifier
                    .width(230.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Entrar", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.width(160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF1C2126),
                    contentColor = Color(0xFFF2F4F6)
                )
            ) {
                Text(text = "Voltar")
            }
        }
    }
}
