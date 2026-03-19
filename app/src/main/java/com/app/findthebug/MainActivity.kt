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
import com.app.findthebug.presentation.victory.VictoryScreen
import com.app.findthebug.presentation.viewmodel.GameViewModel
import com.app.findthebug.presentation.viewmodel.LobbyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionPreferences: SessionPreferences

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

    @Composable
    fun AppRoot(navController: NavHostController = rememberNavController()) {
        val savedSessionId by sessionPreferences.sessionId.collectAsStateWithLifecycle(initialValue = null)
        val savedPlayerName by sessionPreferences.playerName.collectAsStateWithLifecycle(initialValue = null)

        var isNavigating by remember { mutableStateOf(false) }

        val lobbyViewModel: LobbyViewModel = hiltViewModel()
        val gameViewModel: GameViewModel = hiltViewModel()

        LaunchedEffect(Unit) {
            launch {
                lobbyViewModel.navigationEvent.collect { event ->
                    when (event) {
                        is LobbyViewModel.NavigationEvent.GoToHome -> {
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                        }
                        is LobbyViewModel.NavigationEvent.GoToInvestigation -> {
                            navController.navigate(Screen.Investigation.passCaseId(event.caseId)) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                    }
                }
            }

            launch {
                gameViewModel.navigationEvent.collect { event ->
                    when (event) {
                        is GameViewModel.NavigationEvent.GoToVictory -> {
                            navController.navigate(Screen.Victory.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                        is GameViewModel.NavigationEvent.GoToGameOver -> {
                            navController.navigate(Screen.GameOver.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                        is GameViewModel.NavigationEvent.GoToInvestigation -> {
                            navController.navigate(Screen.Investigation.passCaseId(event.caseId)) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                        is GameViewModel.NavigationEvent.GoToHome -> {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            }

            launch {
                gameViewModel.solutionForReview.collect {
                    navController.navigate(Screen.MasterReview.route)
                }
            }
        }



        fun startNewGame() {
            if (isNavigating) return
            isNavigating = true
            navController.navigate(Screen.Lobby.route) {
                popUpTo(Screen.Home.route) { inclusive = false }
            }
            isNavigating = false
        }

        fun resumeSession(sessionId: String, playerName: String) {
            if (isNavigating) return
            isNavigating = true

            lifecycleScope.launch {
                gameViewModel.resetState()

                when (val result = lobbyViewModel.joinLobby(sessionId, playerName)) {
                    is Result.Success -> {
                        val session = result.data
                        if (session.phase != com.app.findthebug.core.common.GamePhase.LOBBY) {
                            val caseId = session.caseId ?: "case_robotics_001"
                            navController.navigate(Screen.Investigation.passCaseId(caseId)) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        } else {
                            navController.navigate(Screen.LobbyRoom.passSessionId(sessionId)) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                    }
                    else -> {
                        sessionPreferences.clearSession()
                        navController.navigate(Screen.Home.route)
                    }
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
                    onResumeSession = { sessionId, playerName -> resumeSession(sessionId, playerName) },
                    onNewGame = { startNewGame() }
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
                CreateLobbyScreen(
                    viewModel = lobbyViewModel,
                    onContinue = { sessionId ->
                        navController.navigate(Screen.LobbyRoom.passSessionId(sessionId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.JoinLobby.route) {
                JoinLobbyScreen(
                    viewModel = lobbyViewModel,
                    onJoinSuccess = { sessionId ->
                        navController.navigate(Screen.LobbyRoom.passSessionId(sessionId))
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
                        navController.navigate(Screen.DebugScenario.route)
                    },
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToInvestigation = { caseId ->
                        navController.navigate(Screen.Investigation.passCaseId(caseId)) {
                            popUpTo(Screen.LobbyRoom.route) { inclusive = true }
                        }
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
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.GameOver.route) {
                GameOverScreen(
                    onBackToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.DebugScenario.route) {
                DebugScenarioScreen(
                    gameViewModel = gameViewModel,
                    lobbyViewModel = lobbyViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToInvestigation = { caseId ->
                        navController.navigate(Screen.Investigation.passCaseId(caseId)) {
                            popUpTo(Screen.LobbyRoom.route) { inclusive = true }
                        }
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

            composable(Screen.Investigation.route) { backStackEntry ->
                val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
                InvestigationScreen(
                    caseId = caseId,
                    gameViewModel = gameViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSubmitSolution = {
                        navController.navigate(Screen.Submission.route)
                    }
                )
            }
        }
    }
}