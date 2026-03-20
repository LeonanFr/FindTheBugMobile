package com.app.findthebug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.findthebug.core.datastore.SessionPreferences
import com.app.findthebug.navigation.Screen
import com.app.findthebug.presentation.cases.CaseDetailScreen
import com.app.findthebug.presentation.gameover.GameOverScreen
import com.app.findthebug.presentation.home.HomeScreen
import com.app.findthebug.presentation.investigation.InvestigationScreen
import com.app.findthebug.presentation.lobby.CreateLobbyScreen
import com.app.findthebug.presentation.lobby.JoinLobbyScreen
import com.app.findthebug.presentation.lobby.LobbyRoomScreen
import com.app.findthebug.presentation.lobby.PlayLobbyScreen
import com.app.findthebug.presentation.master.MasterReviewScreen
import com.app.findthebug.presentation.scenario.DebugScenarioScreen
import com.app.findthebug.presentation.submission.SubmissionScreen
import com.app.findthebug.presentation.victory.VictoryScreen
import com.app.findthebug.presentation.viewmodel.GameViewModel
import com.app.findthebug.presentation.viewmodel.LobbyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionPreferences: SessionPreferences

    @Inject
    lateinit var webSocketService: com.app.findthebug.data.remote.api.WebSocketService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

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

    private suspend fun ensureConnected(): Boolean {
        if (webSocketService.isConnected()) return true

        if (webSocketService.connectionState.value is com.app.findthebug.data.remote.api.WebSocketService.ConnectionState.ERROR) {
            webSocketService.resetConnection()
        }

        webSocketService.connect()
        repeat(50) {
            if (webSocketService.isConnected()) return true
            delay(200)
        }
        return false
    }

    @Composable
    fun AppRoot(navController: NavHostController = rememberNavController()) {

        var isNavigating by remember { mutableStateOf(false) }
        var isRejoining by remember { mutableStateOf(false) }

        val lobbyViewModel: LobbyViewModel = hiltViewModel()
        val gameViewModel: GameViewModel = hiltViewModel()

        fun navigateSafe(route: String, popUpTo: String? = null, inclusive: Boolean = false) {
            if (navController.currentDestination?.route == route) return

            navController.navigate(route) {
                launchSingleTop = true
                popUpTo?.let {
                    popUpTo(it) { this.inclusive = inclusive }
                }
            }
        }

        LaunchedEffect(Unit) {

            launch {
                lobbyViewModel.navigationEvent.collectLatest { event ->
                    if (isRejoining) return@collectLatest

                    val currentRoute = navController.currentDestination?.route

                    when (event) {
                        is LobbyViewModel.NavigationEvent.GoToHome -> {
                            if (currentRoute != Screen.Home.route) {
                                navigateSafe(Screen.Home.route, Screen.Home.route, true)
                            }
                        }

                        is LobbyViewModel.NavigationEvent.GoToInvestigation -> {
                            navigateSafe(
                                Screen.Investigation.passCaseId(event.caseId),
                                Screen.Home.route
                            )
                        }
                    }
                }
            }

            launch {
                gameViewModel.navigationEvent.collectLatest { event ->
                    if (isRejoining) return@collectLatest

                    val currentRoute = navController.currentDestination?.route

                    when (event) {
                        is GameViewModel.NavigationEvent.GoToVictory -> {
                            navigateSafe(Screen.Victory.route, Screen.Home.route)
                        }
                        is GameViewModel.NavigationEvent.GoToGameOver -> {
                            navigateSafe(Screen.GameOver.route, Screen.Home.route)
                        }
                        is GameViewModel.NavigationEvent.GoToInvestigation -> {
                            navigateSafe(
                                Screen.Investigation.passCaseId(event.caseId),
                                Screen.Home.route
                            )
                        }
                        is GameViewModel.NavigationEvent.GoToHome -> {
                            navigateSafe(Screen.Home.route, Screen.Home.route, true)
                        }
                    }
                }
            }

            launch {
                gameViewModel.solutionForReview.collectLatest { reviewData ->
                    if (reviewData != null && !isRejoining) {
                        val myName = gameViewModel.currentPlayerName.value
                        val isMaster = gameViewModel.currentSession.value?.players?.find {
                            it.name.trim().equals(myName?.trim(), ignoreCase = true)
                        }?.role == com.app.findthebug.core.common.PlayerRole.MASTER

                        if (isMaster && navController.currentDestination?.route != Screen.MasterReview.route) {
                            navigateSafe(Screen.MasterReview.route)
                        }
                    }
                }
            }
        }

        fun startNewGame() {
            if (isNavigating) return
            isNavigating = true

            lifecycleScope.launch {
                sessionPreferences.clearSession()
                gameViewModel.resetState()
                lobbyViewModel.resetForNewLobby()
                gameViewModel.resetWebSocket()

                navigateSafe(Screen.Lobby.route, Screen.Home.route)
                isNavigating = false
            }
        }

        fun resumeSession(sessionId: String, playerName: String) {
            if (isNavigating) return

            isNavigating = true
            isRejoining = true

            lifecycleScope.launch {
                try {
                    val isConnected = ensureConnected()

                    if (!isConnected) {
                        sessionPreferences.clearSession()
                        navigateSafe(Screen.Home.route)
                        isRejoining = false
                        return@launch
                    }

                    lobbyViewModel.resetForNewLobby()
                    gameViewModel.setCurrentPlayerName(playerName)

                    lobbyViewModel.joinLobby(sessionId, playerName)

                    val gameState = withTimeoutOrNull(3000) {
                        gameViewModel.currentGameState
                            .drop(1)
                            .filterNotNull()
                            .first()
                    }

                    if (gameState != null) {
                        val caseId =
                            gameViewModel.currentSession.value?.caseId ?: "case_robotics_001"

                        navigateSafe(
                            Screen.Investigation.passCaseId(caseId),
                            Screen.Home.route
                        )
                    } else {
                        navigateSafe(
                            Screen.LobbyRoom.passSessionId(sessionId),
                            Screen.Home.route
                        )
                    }

                    delay(800)
                    isRejoining = false

                } finally {
                    isNavigating = false
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {

            composable(Screen.Home.route) {
                HomeScreen(
                    onPlay = { startNewGame() },
                    onResumeSession = { sessionId, playerName ->
                        resumeSession(
                            sessionId,
                            playerName
                        )
                    },
                    onNewGame = { startNewGame() }
                )
            }

            composable(Screen.Lobby.route) {
                PlayLobbyScreen(
                    onCreateLobby = { navigateSafe(Screen.CreateLobby.route) },
                    onJoinLobby = { navigateSafe(Screen.JoinLobby.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CreateLobby.route) {
                CreateLobbyScreen(
                    viewModel = lobbyViewModel,
                    onContinue = { sessionId ->
                        navigateSafe(Screen.LobbyRoom.passSessionId(sessionId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.JoinLobby.route) {
                JoinLobbyScreen(
                    viewModel = lobbyViewModel,
                    onJoinSuccess = { sessionId ->
                        navigateSafe(Screen.LobbyRoom.passSessionId(sessionId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.LobbyRoom.route) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""

                LobbyRoomScreen(
                    sessionId = sessionId,
                    viewModel = lobbyViewModel,
                    onStartGame = {
                        navigateSafe(Screen.DebugScenario.route)
                    },
                    onNavigateHome = {
                        navigateSafe(Screen.Home.route, Screen.Home.route, true)
                    },
                    onNavigateToInvestigation = { caseId ->
                        navigateSafe(
                            Screen.Investigation.passCaseId(caseId),
                            Screen.LobbyRoom.route,
                            true
                        )
                    }
                )
            }

            composable(Screen.Submission.route) { backStackEntry ->
                val caseId = backStackEntry.arguments?.getString("caseId") ?: ""

                SubmissionScreen(
                    caseId = caseId,
                    gameViewModel = gameViewModel,
                    onBack = { navController.popBackStack() },
                    onSubmit = { navController.popBackStack() }
                )
            }

            composable(Screen.Investigation.route) { backStackEntry ->
                val caseId = backStackEntry.arguments?.getString("caseId") ?: ""

                InvestigationScreen(
                    caseId = caseId,
                    gameViewModel = gameViewModel,
                    onBack = {
                        navigateSafe(Screen.Home.route, Screen.Home.route, true)
                    },
                    onSubmitSolution = { id ->
                        navigateSafe(Screen.Submission.passCaseId(id))
                    },
                    onNavigateHome = {
                        navigateSafe(Screen.Home.route, Screen.Home.route, true)
                    }
                )
            }

            composable(Screen.DebugScenario.route) {
                DebugScenarioScreen(
                    gameViewModel = gameViewModel,
                    lobbyViewModel = lobbyViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToInvestigation = { caseId ->
                        navigateSafe(
                            Screen.Investigation.passCaseId(caseId),
                            Screen.LobbyRoom.route,
                            true
                        )
                    }
                )
            }

            composable(Screen.MasterReview.route) {
                MasterReviewScreen(
                    gameViewModel = gameViewModel,
                    onBack = { navController.popBackStack() },
                    onComplete = { navController.popBackStack() }
                )
            }

            composable(Screen.Victory.route) {
                VictoryScreen(
                    onBackToHome = {
                        navigateSafe(Screen.Home.route, Screen.Home.route, true)
                    }
                )
            }

            composable(Screen.GameOver.route) {
                GameOverScreen(
                    onBackToHome = {
                        navigateSafe(Screen.Home.route, Screen.Home.route, true)
                    }
                )
            }

            composable(Screen.CaseDetail.route) { backStackEntry ->
                val caseId = backStackEntry.arguments?.getString("caseId") ?: ""

                CaseDetailScreen(
                    caseId = caseId,
                    gameViewModel = gameViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}