package com.app.findthebug.presentation.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.findthebug.core.common.PlayerRole
import com.app.findthebug.presentation.viewmodel.LobbyViewModel

@Composable
fun LobbyRoomScreen(
    sessionId: String,
    viewModel: LobbyViewModel,
    onStartGame: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToInvestigation: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.session) {
        android.util.Log.d("LobbyRoom", "Players recebidos: ${uiState.session?.players?.map { it.name }}")
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is LobbyViewModel.NavigationEvent.GoToHome -> {
                    onNavigateHome()
                }
                is LobbyViewModel.NavigationEvent.GoToInvestigation -> {
                    onNavigateToInvestigation(event.caseId)
                }
            }
        }
    }

    if (!uiState.hasLoadedOnce || uiState.session == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1F2429)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF00B7C3))
        }
        return
    }

    val players = uiState.session!!.players
    val master = players.find { it.role == PlayerRole.MASTER }
    val playerCount = players.count { it.role == PlayerRole.PLAYER }
    val canStartGame = playerCount in 1..4
    val isMaster = uiState.currentPlayerName == master?.name

    if (uiState.showLobbyDestroyedDialog) {
        Dialog(onDismissRequest = { viewModel.clearLobbyDestroyedDialog() }) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF232A30), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Lobby encerrado",
                        color = Color(0xFFE9EEF1),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "O mestre saiu e o lobby foi encerrado.",
                        color = Color(0xFFB0B8C0),
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.clearLobbyDestroyedDialog()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B7C3),
                            contentColor = Color.White
                        )
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
    @Composable
    fun Modifier.symmetricSafeDrawing(): Modifier {
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val insets = WindowInsets.safeDrawing

        return with(density) {
            val horizontal = maxOf(
                insets.getLeft(this, layoutDirection),
                insets.getRight(this, layoutDirection)
            ).toDp()

            val vertical = maxOf(
                insets.getTop(this),
                insets.getBottom(this)
            ).toDp()

            this@symmetricSafeDrawing.padding(
                horizontal = horizontal,
                vertical = vertical
            )
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
                .symmetricSafeDrawing()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { viewModel.showExitDialog() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Sair",
                        tint = Color(0xFFE9EEF1)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sala de Espera",
                    color = Color(0xFFE9EEF1),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF151A1F), RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Código: $sessionId",
                        color = Color(0xFFF2F4F6),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Jogadores (${players.size})",
                color = Color(0xFFB0B8C0),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players) { player ->
                    PlayerRow(
                        name = player.name,
                        role = player.role,
                        isCurrent = player.name == uiState.currentPlayerName
                    )
                }
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isMaster) {
                Button(
                    onClick = onStartGame,
                    enabled = canStartGame && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B7C3),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF3A4A53)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = when {
                                playerCount == 0 -> "Aguardando jogadores..."
                                playerCount > 4 -> "Limite de jogadores excedido"
                                else -> "Iniciar Jogo"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFF2E3A44), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aguardando o Mestre iniciar...",
                        color = Color(0xFFA0AAB3),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (uiState.showExitConfirmation) {
        Dialog(onDismissRequest = { viewModel.confirmExit(false) }) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF232A30), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sair da sala",
                        color = Color(0xFFE9EEF1),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tem certeza que deseja sair?",
                        color = Color(0xFFB0B8C0),
                        fontSize = 16.sp
                    )
                    if (isMaster) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ao sair, o lobby será encerrado para todos.",
                            color = Color(0xFFFF6B6B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.confirmExit(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3A4A53),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Não")
                        }
                        Button(
                            onClick = { viewModel.confirmExit(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMaster) Color(0xFFFF6B6B) else Color(0xFF00B7C3),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sim")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerRow(name: String, role: PlayerRole, isCurrent: Boolean) {
    val (icon, tint) = when (role) {
        PlayerRole.MASTER -> Icons.Default.Star to Color(0xFFFFD966)
        else -> Icons.Default.Person to Color(0xFF8AB4F8)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCurrent) Color(0xFF2A3A44) else Color(0xFF232A30),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            color = Color(0xFFE9EEF1),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        if (isCurrent) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(você)",
                color = Color(0xFF8AB4F8),
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = when (role) {
                PlayerRole.MASTER -> "Mestre"
                else -> "Jogador"
            },
            color = Color(0xFF9AA5B0),
            fontSize = 14.sp
        )
    }
}