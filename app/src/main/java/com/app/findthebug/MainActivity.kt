package com.app.findthebug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.findthebug.navigation.Screen
import com.app.findthebug.presentation.cases.CaseDetailScreen
import com.app.findthebug.presentation.home.HomeScreen
import com.app.findthebug.presentation.lobby.CreateLobbyScreen
import com.app.findthebug.presentation.lobby.JoinLobbyScreen
import com.app.findthebug.presentation.lobby.PlayLobbyScreen
import com.app.findthebug.presentation.scenario.DebugScenarioScreen
import com.app.findthebug.presentation.viewmodel.GameViewModel
import com.app.findthebug.presentation.viewmodel.LobbyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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

@Composable
fun AppRoot(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onPlay = { navController.navigate(Screen.Lobby.route) }
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
                onContinue = { navController.navigate(Screen.DebugScenario.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.JoinLobby.route) {
            val viewModel: LobbyViewModel = hiltViewModel()
            JoinLobbyScreen(
                viewModel = viewModel,
                onJoinSuccess = { navController.navigate(Screen.DebugScenario.route) },
                onBack = { navController.popBackStack() }
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