package com.app.findthebug.domain.repository

import com.app.findthebug.core.common.Result
import com.app.findthebug.data.remote.model.websocket.WebSocketMessage
import com.app.findthebug.domain.model.GameState
import com.app.findthebug.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface IGameRepository {
    // Lobby
    suspend fun createLobby(playerName: String): Result<Session>
    suspend fun joinLobby(sessionId: String, playerName: String): Result<Session>
    suspend fun joinAsMaster(sessionId: String, masterName: String): Result<Session>
    suspend fun getLobbyInfo(sessionId: String): Result<Session>
    suspend fun startGame(sessionId: String, playerName: String, caseId: String = "case_robotics_001"): Result<Unit>

    // Game Actions
    suspend fun executeAction(sessionId: String, playerId: String, actionType: Int, targetId: String): Result<GameState>
    suspend fun submitSolution(sessionId: String, answers: List<String>): Result<Unit>
    suspend fun saveNote(sessionId: String, playerId: String, clueId: String, content: String): Result<Unit>
    suspend fun validateSolution(sessionId: String, approved: Boolean): Result<Unit>


    // Real-time Updates
    fun observeGameState(sessionId: String): Flow<GameState?>
    fun observeSession(sessionId: String): Flow<Session?>
    fun observeMessages(): Flow<WebSocketMessage>

    // Connection
    fun connectWebSocket()
    fun disconnectWebSocket()
    fun isConnected(): Boolean

    // Leave
    suspend fun leaveLobby()
}