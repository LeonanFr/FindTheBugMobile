package com.app.findthebug.presentation.scenario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.findthebug.presentation.components.BackButton
import com.app.findthebug.presentation.viewmodel.CasesViewModel
import com.app.findthebug.presentation.viewmodel.GameViewModel
import com.app.findthebug.presentation.viewmodel.LobbyViewModel

data class DebugCase(
    val id: String,
    val title: String,
    val subtitle: String
)

@Composable
fun DebugScenarioScreen(
    gameViewModel: GameViewModel,
    lobbyViewModel: LobbyViewModel,
    onBack: () -> Unit,
    onNavigateToInvestigation: (String) -> Unit
) {
    val casesViewModel: CasesViewModel = hiltViewModel()
    val cases by casesViewModel.cases.collectAsStateWithLifecycle()
    val isLoadingCases by casesViewModel.isLoading.collectAsStateWithLifecycle()
    val errorCases by casesViewModel.errorMessage.collectAsStateWithLifecycle()

    val gameIsLoading by gameViewModel.isLoading.collectAsStateWithLifecycle()
    val lobbyUiState by lobbyViewModel.uiState.collectAsStateWithLifecycle()
    val currentPlayerName by gameViewModel.currentPlayerName.collectAsStateWithLifecycle()

    val isMaster = lobbyUiState.session?.masterPlayerId == currentPlayerName

    LaunchedEffect(Unit) {
        casesViewModel.loadCases()
    }

    LaunchedEffect(Unit) {
        gameViewModel.gameStarted.collect { caseId ->
            onNavigateToInvestigation(caseId)
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
            BackButton(onClick = onBack)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selecionar Caso",
                color = Color(0xFFE9EEF1),
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoadingCases) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00B7C3))
                }
            } else if (!errorCases.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorCases!!,
                        color = Color(0xFFFF6B6B),
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else if (cases.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum caso disponível",
                        color = Color(0xFF8A9095),
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cases) { case ->
                        DebugCaseCard(
                            case = case,
                            isEnabled = isMaster && !gameIsLoading,
                            onInvestigate = {
                                if (isMaster) {
                                    gameViewModel.startGame(case.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebugCaseCard(
    case: DebugCase,
    isEnabled: Boolean,
    onInvestigate: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(Color(0xFF232A30), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = case.title,
            color = Color(0xFFE9EEF1),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = case.subtitle,
            color = Color(0xFF8A9095),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onInvestigate,
            enabled = isEnabled,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00B7C3),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF3A4A53)
            )
        ) {
            Text("Iniciar", fontWeight = FontWeight.Bold)
        }
    }
}