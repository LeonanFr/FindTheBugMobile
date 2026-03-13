package com.app.findthebug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.findthebug.core.common.Result
import com.app.findthebug.core.datastore.SessionPreferences
import com.app.findthebug.domain.usecase.session.ValidateSessionUseCase
import com.app.findthebug.navigation.Screen
import com.app.findthebug.presentation.cases.CaseDetailScreen
import com.app.findthebug.presentation.home.HomeScreen
import com.app.findthebug.presentation.lobby.CreateLobbyScreen
import com.app.findthebug.presentation.lobby.JoinLobbyScreen
import com.app.findthebug.presentation.lobby.LobbyRoomScreen
import com.app.findthebug.presentation.lobby.PlayLobbyScreen
import com.app.findthebug.presentation.scenario.DebugScenarioScreen
import com.app.findthebug.presentation.viewmodel.GameViewModel
import com.app.findthebug.presentation.viewmodel.LobbyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionPreferences: SessionPreferences

    @Inject
    lateinit var validateSessionUseCase: ValidateSessionUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        lifecycleScope.launch {
            val sessionId = sessionPreferences.sessionId.first()
            val playerName = sessionPreferences.playerName.first()
            val active = sessionPreferences.isSessionActive.first()

            if (active && sessionId != null && playerName != null) {
                val isValid = when (val result = validateSessionUseCase(sessionId, playerName)) {
                    is Result.Success -> result.data
                    else -> false
                }
                if (!isValid) {
                    sessionPreferences.clearSession()
                }
            }
        }

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

    @Composable
    fun AppRoot(navController: NavHostController = rememberNavController()) {
        val hasActiveSession by sessionPreferences.isSessionActive.collectAsStateWithLifecycle(initialValue = false)
        val savedSessionId by sessionPreferences.sessionId.collectAsStateWithLifecycle(initialValue = null)
        val savedPlayerName by sessionPreferences.playerName.collectAsStateWithLifecycle(initialValue = null)

        var isNavigating by remember { mutableStateOf(false) }

        fun startNewGame() {
            if (isNavigating) return
            isNavigating = true
            lifecycleScope.launch {
                sessionPreferences.clearSession()
                navController.navigate(Screen.Lobby.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                }
                isNavigating = false
            }
        }

        fun resumeSession() {
            if (isNavigating) return
            if (savedSessionId != null && savedPlayerName != null) {
                isNavigating = true
                navController.navigate(Screen.LobbyRoom.passSessionId(savedSessionId!!)) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                }
                isNavigating = false
            }
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onPlay = { startNewGame() },
                    onResumeSession = { _, _ -> resumeSession() },
                    onNewGame = { startNewGame() },
                    hasActiveSession = hasActiveSession
                )
            }

            composable(Screen.Lobby.route) {
                PlayLobbyScreen(
                    onCreateLobby = { navController.navigate(Screen.CreateLobby.route) },
                    onJoinLobby = { navController.navigate(Screen.JoinLobby.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CreateLobby.route) {
                val viewModel: LobbyViewModel = hiltViewModel()
                CreateLobbyScreen(
                    viewModel = viewModel,
                    onContinue = { sessionId ->
                        navController.navigate(Screen.LobbyRoom.passSessionId(sessionId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.JoinLobby.route) {
                val viewModel: LobbyViewModel = hiltViewModel()
                JoinLobbyScreen(
                    viewModel = viewModel,
                    onJoinSuccess = { sessionId ->
                        navController.navigate(Screen.LobbyRoom.passSessionId(sessionId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.LobbyRoom.route) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                val viewModel: LobbyViewModel = hiltViewModel()
                LaunchedEffect(sessionId) {
                    viewModel.startObservingSession(sessionId)
                    viewModel.getLobbyInfo(sessionId)
                }
                LobbyRoomScreen(
                    sessionId = sessionId,
                    viewModel = viewModel,
                    onStartGame = {
                        navController.navigate(Screen.DebugScenario.route)
                    },
                    onLobbyClosed = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                )
            }

            composable(Screen.DebugScenario.route) {
                val gameViewModel: GameViewModel = hiltViewModel()
                val lobbyViewModel: LobbyViewModel = hiltViewModel()
                DebugScenarioScreen(
                    gameViewModel = gameViewModel,
                    lobbyViewModel = lobbyViewModel,
                    onInvestigate = { case ->
                        navController.navigate(Screen.CaseDetail.passCaseId(case.id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CaseDetail.route) { backStackEntry ->
                val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
                val gameViewModel: GameViewModel = hiltViewModel()
                CaseDetailScreen(
                    caseId = caseId,
                    gameViewModel = gameViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}