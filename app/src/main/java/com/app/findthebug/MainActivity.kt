package com.app.findthebug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.app.findthebug.core.common.PlayerRole
import com.app.findthebug.core.common.Result
import com.app.findthebug.data.remote.api.ApiClient
import com.app.findthebug.data.remote.api.WebSocketService
import com.app.findthebug.data.repository.CaseRepositoryImpl
import com.app.findthebug.data.repository.GameRepositoryImpl
import com.app.findthebug.domain.model.BugCase
import com.app.findthebug.domain.model.GameState
import com.app.findthebug.domain.model.Session
import com.app.findthebug.presentation.case.CaseDetailScreen
import com.app.findthebug.presentation.case.CaseSection
import com.app.findthebug.presentation.home.HomeScreen
import com.app.findthebug.presentation.lobby.CreateLobbyScreen
import com.app.findthebug.presentation.lobby.JoinLobbyScreen
import com.app.findthebug.presentation.lobby.PlayLobbyScreen
import com.app.findthebug.presentation.scenario.DebugCase
import com.app.findthebug.presentation.scenario.DebugScenarioScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot()
                }
            }
        }
    }
}

private enum class AppScreen {
    HOME,
    LOBBY,
    CREATE_LOBBY,
    JOIN_LOBBY,
    DEBUG_SCENARIO,
    CASE_DETAIL
}

@Composable
private fun AppRoot() {
    var screen by remember { mutableStateOf(AppScreen.HOME) }
    var selectedCase by remember { mutableStateOf<BugCase?>(null) }
    var createPlayerName by remember { mutableStateOf("") }
    var joinPlayerName by remember { mutableStateOf("") }
    var joinLobbyCode by remember { mutableStateOf("") }
    var joinRequested by remember { mutableStateOf(false) }
    var session by remember { mutableStateOf<Session?>(null) }
    var gameState by remember { mutableStateOf<GameState?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var cases by remember { mutableStateOf<List<DebugCase>>(emptyList()) }
    var isLoadingCases by remember { mutableStateOf(false) }
    var caseError by remember { mutableStateOf<String?>(null) }

    val webSocketService = remember { WebSocketService() }
    val gameRepository = remember { GameRepositoryImpl(webSocketService) }
    val caseRepository = remember { CaseRepositoryImpl(ApiClient) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            gameRepository.disconnectWebSocket()
        }
    }

    LaunchedEffect(screen, session?.sessionId, joinRequested) {
        if (screen == AppScreen.JOIN_LOBBY && joinRequested && session?.sessionId != null) {
            joinRequested = false
            screen = AppScreen.DEBUG_SCENARIO
        }
    }

    LaunchedEffect(session?.sessionId) {
        val sid = session?.sessionId ?: return@LaunchedEffect
        gameRepository.observeGameState(sid).collect { state ->
            gameState = state
        }
    }

    LaunchedEffect(screen) {
        if (screen == AppScreen.DEBUG_SCENARIO && cases.isEmpty()) {
            isLoadingCases = true
            caseError = null
            when (val result = caseRepository.getCases()) {
                is Result.Success -> {
                    cases = result.data.map {
                        DebugCase(
                            id = it.id,
                            title = it.title,
                            subtitle = it.shortDescription.ifBlank { it.description.ifBlank { "No description" } }
                        )
                    }
                }
                is Result.Error -> {
                    caseError = result.message
                }
                else -> {}
            }
            isLoadingCases = false
        }
    }

    when (screen) {
        AppScreen.HOME -> HomeScreen(
            onPlay = { screen = AppScreen.LOBBY }
        )

        AppScreen.LOBBY -> PlayLobbyScreen(
            onCreateLobby = {
                errorMessage = null
                screen = AppScreen.CREATE_LOBBY
            },
            onJoinLobby = {
                errorMessage = null
                screen = AppScreen.JOIN_LOBBY
            },
            onBack = {
                errorMessage = null
                screen = AppScreen.HOME
            }
        )

        AppScreen.CREATE_LOBBY -> CreateLobbyScreen(
            playerName = createPlayerName,
            onPlayerNameChange = { createPlayerName = it },
            lobbyCode = session?.sessionId,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onCreateLobby = {
                errorMessage = null
                isLoading = true
                scope.launch {
                    when (val result = gameRepository.createLobby(createPlayerName.trim())) {
                        is Result.Success -> {
                            session = result.data
                        }
                        is Result.Error -> {
                            errorMessage = result.message
                        }
                        else -> {}
                    }
                    isLoading = false
                }
            },
            onContinue = { screen = AppScreen.DEBUG_SCENARIO },
            onBack = {
                errorMessage = null
                screen = AppScreen.LOBBY
            }
        )

        AppScreen.JOIN_LOBBY -> JoinLobbyScreen(
            playerName = joinPlayerName,
            onPlayerNameChange = { joinPlayerName = it },
            lobbyCode = joinLobbyCode,
            onLobbyCodeChange = { joinLobbyCode = it },
            isLoading = isLoading,
            errorMessage = errorMessage,
            onJoin = {
                errorMessage = null
                joinRequested = true
                isLoading = true
                scope.launch {
                    when (val result = gameRepository.joinLobby(joinLobbyCode.trim(), joinPlayerName.trim())) {
                        is Result.Success -> {
                            session = result.data
                        }
                        is Result.Error -> {
                            joinRequested = false
                            errorMessage = result.message
                        }
                        else -> {}
                    }
                    isLoading = false
                }
            },
            onBack = {
                errorMessage = null
                screen = AppScreen.LOBBY
            }
        )

        AppScreen.DEBUG_SCENARIO -> DebugScenarioScreen(
            cases = cases,
            isLoading = isLoadingCases || isLoading,
            errorMessage = caseError ?: errorMessage,
            onBack = { screen = AppScreen.LOBBY },
            onInvestigate = { chosen ->
                isLoading = true
                errorMessage = null
                scope.launch {
                    val currentSession = session
                    val currentPlayer = currentSession?.players?.firstOrNull()

                    if (currentSession != null && currentPlayer?.role == PlayerRole.HOST) {
                        when (val startResult = gameRepository.startGame(
                            currentSession.sessionId,
                            currentPlayer.name,
                            chosen.id
                        )) {
                            is Result.Error -> {
                                errorMessage = startResult.message
                                isLoading = false
                                return@launch
                            }
                            else -> {}
                        }
                    }

                    when (val detailsResult = caseRepository.getCaseDetails(chosen.id)) {
                        is Result.Success -> {
                            selectedCase = detailsResult.data
                            screen = AppScreen.CASE_DETAIL
                        }
                        is Result.Error -> {
                            errorMessage = detailsResult.message
                        }
                        else -> {}
                    }

                    isLoading = false
                }
            }
        )

        AppScreen.CASE_DETAIL -> CaseDetailScreen(
            title = selectedCase?.title ?: "Case",
            scorePoints = gameState?.remainingPoints ?: 12,
            daysLeft = (6 - (gameState?.currentDay ?: 1)).coerceAtLeast(0),
            sections = selectedCase?.systemTopology?.modules
                ?.map { CaseSection(it.name) }
                ?.ifEmpty { listOf(CaseSection("No modules to show")) }
                ?: listOf(CaseSection("No case data")),
            onBack = { screen = AppScreen.DEBUG_SCENARIO },
            onDeclareSolution = { }
        )
    }
}
