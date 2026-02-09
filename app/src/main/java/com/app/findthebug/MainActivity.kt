package com.app.findthebug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.app.findthebug.presentation.home.HomeScreen
import com.app.findthebug.presentation.lobby.CreateLobbyScreen
import com.app.findthebug.presentation.lobby.JoinLobbyScreen
import com.app.findthebug.presentation.lobby.PlayLobbyScreen
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

private enum class AppScreen {
    HOME,
    LOBBY,
    CREATE_LOBBY,
    JOIN_LOBBY
}

@Composable
private fun AppRoot() {
    var screen by remember { mutableStateOf(AppScreen.HOME) }
    val mockLobbyCode = "FTB-9X42"

    when (screen) {
        AppScreen.HOME -> HomeScreen(
            onPlay = { screen = AppScreen.LOBBY }
        )
        AppScreen.LOBBY -> PlayLobbyScreen(
            onCreateLobby = { screen = AppScreen.CREATE_LOBBY },
            onJoinLobby = { screen = AppScreen.JOIN_LOBBY },
            onBack = { screen = AppScreen.HOME }
        )
        AppScreen.CREATE_LOBBY -> CreateLobbyScreen(
            lobbyCode = mockLobbyCode,
            onShare = { },
            onBack = { screen = AppScreen.LOBBY }
        )
        AppScreen.JOIN_LOBBY -> JoinLobbyScreen(
            onJoin = { screen = AppScreen.LOBBY },
            onBack = { screen = AppScreen.LOBBY }
        )
    }
}
