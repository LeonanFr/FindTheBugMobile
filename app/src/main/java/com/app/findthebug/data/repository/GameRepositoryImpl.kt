package com.app.findthebug.data.repository

import android.util.Log
import com.app.findthebug.core.common.Result
import com.app.findthebug.data.remote.api.WebSocketService
import com.app.findthebug.data.remote.model.websocket.*
import com.app.findthebug.domain.model.*
import com.app.findthebug.domain.repository.IGameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val webSocketService: WebSocketService
) : IGameRepository {

    private val currentGameState = MutableStateFlow<GameState?>(null)
    private val currentSession = MutableStateFlow<Session?>(null)

    private suspend fun ensureConnected(): Boolean {
        if (webSocketService.isConnected()) return true
        webSocketService.connect()
        repeat(50) {
            if (webSocketService.isConnected()) return true
            delay(200)
        }
        return false
    }

    override suspend fun leaveLobbyBySession(sessionId: String, playerName: String) {
        try {
            ensureConnected()
            val request = WebSocketMessage.LeaveLobbyRequest(sessionId = sessionId, playerName =  playerName)
            webSocketService.sendMessage(request)
        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
        }
    }
    override suspend fun createLobby(playerName: String): Result<Session> {
        return try {
            if (!ensureConnected()) return Result.Error("WebSocket not connected")
            val request = WebSocketMessage.CreateLobbyRequest(playerName = playerName)
            when (val result = webSocketService.sendMessageAndWaitForResponse<WebSocketMessage.LobbyCreatedResponse>(request)) {
                is Result.Success -> {
                    val response = result.data
                    val player = Player(name = response.playerName, role = com.app.findthebug.core.common.PlayerRole.fromInt(response.role))
                    val session = Session(
                        sessionId = response.sessionId,
                        players = listOf(player),
                        masterPlayerId = if (player.role == com.app.findthebug.core.common.PlayerRole.MASTER) player.name else null
                    )
                    currentSession.value = session
                    Result.Success(session)
                }
                is Result.Error -> result
                else -> Result.Error("Unexpected result type")
            }
        } catch (e: Exception) {
            Result.Error("Create lobby failed: ${e.message}", e)
        }
    }

    override suspend fun joinLobby(sessionId: String, playerName: String): Result<Session> {
        return try {
            if (!ensureConnected()) return Result.Error("WebSocket not connected")
            val request = WebSocketMessage.JoinAsPlayerRequest(sessionId = sessionId, playerName = playerName)
            when (val result = webSocketService.sendMessageAndWaitForResponse<WebSocketMessage.JoinedLobbyResponse>(request)) {
                is Result.Success -> {
                    val response = result.data
                    val resolvedRole = com.app.findthebug.core.common.PlayerRole.fromInt(response.role ?: com.app.findthebug.core.common.PlayerRole.PLAYER.value)
                    val selfPlayer = Player(name = playerName, role = resolvedRole)
                    val session = Session(
                        sessionId = response.sessionId,
                        players = listOf(selfPlayer),
                        masterPlayerId = if (resolvedRole == com.app.findthebug.core.common.PlayerRole.MASTER) playerName else null
                    )
                    currentSession.value = session
                    Result.Success(session)
                }
                is Result.Error -> result
                else -> Result.Error("Unexpected result type")
            }
        } catch (e: Exception) {
            Result.Error("Join lobby failed: ${e.message}", e)
        }
    }

    override suspend fun getLobbyInfo(sessionId: String): Result<Session> {
        return try {
            if (!ensureConnected()) return Result.Error("WebSocket not connected")
            val request = WebSocketMessage.GetLobbyInfoRequest(sessionId = sessionId)
            when (val result = webSocketService.sendMessageAndWaitForResponse<WebSocketMessage.LobbyInfoResponse>(request, timeoutMillis = 5000)) {
                is Result.Success -> {
                    val response = result.data
                    if (response.exists && response.players != null) {
                        val players = response.players.map { playerDto ->
                            Player(name = playerDto.name, role = com.app.findthebug.core.common.PlayerRole.fromInt(playerDto.role))
                        }
                        val session = Session(
                            sessionId = sessionId,
                            players = players,
                            masterPlayerId = players.find { it.role == com.app.findthebug.core.common.PlayerRole.MASTER }?.name
                        )
                        currentSession.value = session
                        Result.Success(session)
                    } else {
                        Result.Error("Lobby not found")
                    }
                }
                is Result.Error -> result
                else -> Result.Error("Unexpected result type")
            }
        } catch (e: Exception) {
            Result.Error("Get lobby info failed: ${e.message}", e)
        }
    }

    override suspend fun startGame(sessionId: String, playerName: String, caseId: String): Result<Unit> {
        return try {
            if (!ensureConnected()) return Result.Error("WebSocket not connected")
            val request = WebSocketMessage.StartGameRequest(sessionId = sessionId, playerName = playerName, caseId = caseId)
            when (val result = webSocketService.sendMessageAndWaitForResponse<WebSocketMessage.GameStartedResponse>(request)) {
                is Result.Success -> Result.Success(Unit)
                is Result.Error -> result
                else -> Result.Error("Unexpected result type")
            }
        } catch (e: Exception) {
            Result.Error("Start game failed: ${e.message}", e)
        }
    }

    override suspend fun executeAction(sessionId: String, playerId: String, actionType: Int, targetId: String): Result<GameState> {
        return try {
            if (!ensureConnected()) return Result.Error("WebSocket not connected")
            val request = WebSocketMessage.GameActionRequest(
                sessionId = sessionId,
                playerId = playerId,
                actionType = actionType,
                targetId = targetId
            )
            when (val result = webSocketService.sendMessageAndWaitForResponse<WebSocketMessage.GameStateUpdateResponse>(request)) {
                is Result.Success -> {
                    val gameState = convertToGameState(result.data)
                    currentGameState.value = gameState
                    Result.Success(gameState)
                }
                is Result.Error -> result
                else -> Result.Error("Unexpected result type")
            }
        } catch (e: Exception) {
            Result.Error("Execute action failed: ${e.message}", e)
        }
    }

    override suspend fun submitSolution(sessionId: String, answers: List<String>): Result<Unit> {
        return try {
            if (!ensureConnected()) return Result.Error("WebSocket not connected")
            val request = WebSocketMessage.SubmitSolutionRequest(sessionId = sessionId, answers = answers)
            if (!webSocketService.sendMessage(request)) return Result.Error("Failed to send submit solution request")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Submit solution failed: ${e.message}", e)
        }
    }

    override suspend fun saveNote(sessionId: String, playerId: String, clueId: String, content: String): Result<Unit> {
        return try {
            if (!ensureConnected()) return Result.Error("WebSocket not connected")
            val request = WebSocketMessage.SaveNoteRequest(sessionId = sessionId, playerId = playerId, clueId = clueId, content = content)
            if (!webSocketService.sendMessage(request)) return Result.Error("Failed to send save note request")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Save note failed: ${e.message}", e)
        }
    }

    override suspend fun validateSolution(sessionId: String, approved: Boolean): Result<Unit> {
        return try {
            if (!ensureConnected()) return Result.Error("WebSocket not connected")
            val request = WebSocketMessage.ValidateSolutionRequest(sessionId = sessionId, approved = approved)
            if (!webSocketService.sendMessage(request)) return Result.Error("Failed to send validate solution request")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Validate solution failed: ${e.message}", e)
        }
    }

    override suspend fun leaveLobby(sessionId: String, playerName: String) {
        try {
            if (webSocketService.isConnected()) {
                val request = WebSocketMessage.LeaveLobbyRequest(
                    sessionId = sessionId,
                    playerName = playerName
                )
                webSocketService.sendMessage(request)
            }
        } catch (e: Exception) {
            Log.e("GameRepository", "Error in leaveLobby", e)
        } finally {
            currentSession.value = null
            currentGameState.value = null
        }
    }

    override fun observeGameState(sessionId: String): Flow<GameState?> {
        return webSocketService.messages
            .filter { it is WebSocketMessage.GameStateUpdateResponse && it.sessionId == sessionId }
            .map { message ->
                val response = message as WebSocketMessage.GameStateUpdateResponse
                convertToGameState(response).also { currentGameState.value = it }
            }
    }

    override fun observeSession(sessionId: String): Flow<Session?> {
        return webSocketService.messages
            .filter {
                val accept = (it is WebSocketMessage.LobbyUpdateResponse && it.sessionId == sessionId) ||
                        (it is WebSocketMessage.LobbyInfoResponse && it.sessionId == sessionId) ||
                        (it is WebSocketMessage.LobbyDestroyedResponse)
                if (accept) {
                    Log.d("WebSocket", "Mensagem de sessão recebida: ${it.type} para $sessionId")
                }
                accept
            }
            .map { message ->
                when (message) {
                    is WebSocketMessage.LobbyUpdateResponse -> {
                        val players = message.players.map { playerDto ->
                            Player(name = playerDto.name, role = com.app.findthebug.core.common.PlayerRole.fromInt(playerDto.role))
                        }
                        Log.d("GameRepository", "LobbyUpdate -> Session: players=${players.map { it.name }}")

                        Session(
                            sessionId = message.sessionId,
                            players = players,
                            masterPlayerId = players.find { it.role == com.app.findthebug.core.common.PlayerRole.MASTER }?.name,
                            canStart = message.canStart
                        ).also { currentSession.value = it }

                    }
                    is WebSocketMessage.LobbyInfoResponse -> {
                        if (message.exists && message.players != null) {
                            val players = message.players.map { playerDto ->
                                Player(name = playerDto.name, role = com.app.findthebug.core.common.PlayerRole.fromInt(playerDto.role))
                            }
                            Session(
                                sessionId = message.sessionId!!,
                                players = players,
                                masterPlayerId = players.find { it.role == com.app.findthebug.core.common.PlayerRole.MASTER }?.name
                            ).also { currentSession.value = it }
                        } else {
                            null
                        }
                    }
                    is WebSocketMessage.LobbyDestroyedResponse -> {
                        null
                    }
                    else -> null
                }
            }
    }

    override fun observeMessages(): Flow<WebSocketMessage> = webSocketService.messages

    override fun connectWebSocket() = webSocketService.connect()

    override fun disconnectWebSocket() = webSocketService.disconnect()

    override fun isConnected(): Boolean = webSocketService.isConnected()

    private fun convertToGameState(response: WebSocketMessage.GameStateUpdateResponse): GameState {
        return GameState(
            sessionId = response.sessionId,
            currentDay = response.currentDay,
            remainingPoints = response.remainingPoints,
            currentTurnPlayer = response.currentTurnPlayer,
            discoveredClues = response.discoveredClues.map { dto ->
                Clue(
                    id = dto.id,
                    targetId = dto.targetId,
                    type = com.app.findthebug.core.common.ClueType.fromInt(dto.type),
                    content = dto.content,
                    playerNotes = dto.playerNotes
                )
            }
        )
    }
}