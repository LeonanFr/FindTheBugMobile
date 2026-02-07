package com.app.findthebug.data.repository

import com.app.findthebug.core.common.Result
import com.app.findthebug.data.remote.api.WebSocketService
import com.app.findthebug.data.remote.model.websocket.*
import com.app.findthebug.domain.model.*
import com.app.findthebug.domain.repository.IGameRepository
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

    override suspend fun createLobby(playerName: String): Result<Session> {
        return try {
            val request = WebSocketMessage.CreateLobbyRequest(
                playerName = playerName
            )
            if (!webSocketService.sendMessage(request)) {
                return Result.Error("Failed to send create lobby request")
            }

            val response = webSocketService.waitForMessage<WebSocketMessage.LobbyCreatedResponse>(
                predicate = { it.playerName == playerName },
                timeoutMillis = 10000
            )

            response?.let {
                val player = Player(
                    name = it.playerName,
                    role = com.app.findthebug.core.common.PlayerRole.fromInt(it.role)
                )
                val session = Session(
                    sessionId = it.sessionId,
                    players = listOf(player),
                    hostPlayerId = if (player.role == com.app.findthebug.core.common.PlayerRole.HOST) player.name else null
                )
                currentSession.value = session
                Result.Success(session)
            } ?: Result.Error("No response received for lobby creation")
        } catch (e: Exception) {
            Result.Error("Create lobby failed: ${e.message}", e)
        }
    }

    override suspend fun joinLobby(sessionId: String, playerName: String): Result<Session> {
        return try {
            val request = WebSocketMessage.JoinAsPlayerRequest(
                sessionId = sessionId,
                playerName = playerName
            )
            if (!webSocketService.sendMessage(request)) {
                return Result.Error("Failed to send join lobby request")
            }

            val response = webSocketService.waitForMessage<WebSocketMessage.JoinedLobbyResponse>(
                predicate = { it.sessionId == sessionId && it.playerName == playerName },
                timeoutMillis = 10000
            )

            response?.let {
                val session = Session(
                    sessionId = it.sessionId,
                    players = emptyList()
                )
                currentSession.value = session
                Result.Success(session)
            } ?: Result.Error("No response received for joining lobby")
        } catch (e: Exception) {
            Result.Error("Join lobby failed: ${e.message}", e)
        }
    }

    override suspend fun joinAsMaster(sessionId: String, masterName: String): Result<Session> {
        return try {
            val request = WebSocketMessage.JoinAsMasterRequest(
                sessionId = sessionId,
                masterName = masterName
            )
            if (!webSocketService.sendMessage(request)) {
                return Result.Error("Failed to send join as master request")
            }

            val response = webSocketService.waitForMessage<WebSocketMessage.JoinedLobbyResponse>(
                predicate = { it.sessionId == sessionId && it.playerName == masterName },
                timeoutMillis = 10000
            )

            response?.let {
                val player = Player(
                    name = it.playerName,
                    role = com.app.findthebug.core.common.PlayerRole.MASTER
                )
                val session = Session(
                    sessionId = it.sessionId,
                    players = listOf(player),
                    masterPlayerId = player.name
                )
                currentSession.value = session
                Result.Success(session)
            } ?: Result.Error("No response received for joining as master")
        } catch (e: Exception) {
            Result.Error("Join as master failed: ${e.message}", e)
        }
    }

    override suspend fun getLobbyInfo(sessionId: String): Result<Session> {
        return try {
            val request = WebSocketMessage.GetLobbyInfoRequest(
                sessionId = sessionId
            )
            if (!webSocketService.sendMessage(request)) {
                return Result.Error("Failed to send lobby info request")
            }

            val response = webSocketService.waitForMessage<WebSocketMessage.LobbyInfoResponse>(
                predicate = { it.sessionId == sessionId },
                timeoutMillis = 5000
            )

            response?.let {
                if (it.exists && it.players != null) {
                    val players = it.players.map { playerDto ->
                        Player(
                            name = playerDto.name,
                            role = com.app.findthebug.core.common.PlayerRole.fromInt(playerDto.role)
                        )
                    }
                    val session = Session(
                        sessionId = sessionId,
                        players = players,
                        hostPlayerId = players.find { it.role == com.app.findthebug.core.common.PlayerRole.HOST }?.name,
                        masterPlayerId = players.find { it.role == com.app.findthebug.core.common.PlayerRole.MASTER }?.name
                    )
                    currentSession.value = session
                    Result.Success(session)
                } else {
                    Result.Error("Lobby not found")
                }
            } ?: Result.Error("No response received for lobby info")
        } catch (e: Exception) {
            Result.Error("Get lobby info failed: ${e.message}", e)
        }
    }

    override suspend fun startGame(sessionId: String, playerName: String, caseId: String): Result<Unit> {
        return try {
            val request = WebSocketMessage.StartGameRequest(
                sessionId = sessionId,
                playerName = playerName,
                caseId = caseId
            )
            if (!webSocketService.sendMessage(request)) {
                return Result.Error("Failed to send start game request")
            }

            val response = webSocketService.waitForMessage<WebSocketMessage.GameStartedResponse>(
                predicate = { it.sessionId == sessionId },
                timeoutMillis = 10000
            )

            if (response != null) {
                Result.Success(Unit)
            } else {
                Result.Error("No response received for starting game")
            }
        } catch (e: Exception) {
            Result.Error("Start game failed: ${e.message}", e)
        }
    }

    override suspend fun executeAction(sessionId: String, playerId: String, actionType: Int, targetId: String): Result<GameState> {
        return try {
            val request = WebSocketMessage.GameActionRequest(
                sessionId = sessionId,
                playerId = playerId,
                actionType = actionType,
                targetId = targetId
            )
            if (!webSocketService.sendMessage(request)) {
                return Result.Error("Failed to send game action request")
            }

            val response = webSocketService.waitForMessage<WebSocketMessage.GameStateUpdateResponse>(
                predicate = { it.sessionId == sessionId },
                timeoutMillis = 10000
            )

            response?.let {
                val gameState = convertToGameState(it)
                currentGameState.value = gameState
                Result.Success(gameState)
            } ?: Result.Error("No response received for game action")
        } catch (e: Exception) {
            Result.Error("Execute action failed: ${e.message}", e)
        }
    }

    override suspend fun submitSolution(sessionId: String, answers: List<String>): Result<Unit> {
        return try {
            val request = WebSocketMessage.SubmitSolutionRequest(
                sessionId = sessionId,
                answers = answers
            )
            if (!webSocketService.sendMessage(request)) {
                return Result.Error("Failed to send submit solution request")
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Submit solution failed: ${e.message}", e)
        }
    }

    override suspend fun saveNote(sessionId: String, playerId: String, clueId: String, content: String): Result<Unit> {
        return try {
            val request = WebSocketMessage.SaveNoteRequest(
                sessionId = sessionId,
                playerId = playerId,
                clueId = clueId,
                content = content
            )
            if (!webSocketService.sendMessage(request)) {
                return Result.Error("Failed to send save note request")
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Save note failed: ${e.message}", e)
        }
    }

    override suspend fun validateSolution(sessionId: String, approved: Boolean): Result<Unit> {
        return try {
            val request = WebSocketMessage.ValidateSolutionRequest(
                sessionId = sessionId,
                approved = approved
            )
            if (!webSocketService.sendMessage(request)) {
                return Result.Error("Failed to send validate solution request")
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Validate solution failed: ${e.message}", e)
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
                (it is WebSocketMessage.LobbyUpdateResponse && it.sessionId == sessionId) ||
                        (it is WebSocketMessage.LobbyInfoResponse && it.sessionId == sessionId)
            }
            .map { message ->
                when (message) {
                    is WebSocketMessage.LobbyUpdateResponse -> {
                        val players = message.players.map { playerDto ->
                            Player(
                                name = playerDto.name,
                                role = com.app.findthebug.core.common.PlayerRole.fromInt(playerDto.role)
                            )
                        }
                        Session(
                            sessionId = message.sessionId,
                            players = players,
                            hostPlayerId = players.find { it.role == com.app.findthebug.core.common.PlayerRole.HOST }?.name,
                            masterPlayerId = players.find { it.role == com.app.findthebug.core.common.PlayerRole.MASTER }?.name,
                            canStart = message.canStart
                        ).also { currentSession.value = it }
                    }
                    is WebSocketMessage.LobbyInfoResponse -> {
                        if (message.exists && message.players != null) {
                            val players = message.players.map { playerDto ->
                                Player(
                                    name = playerDto.name,
                                    role = com.app.findthebug.core.common.PlayerRole.fromInt(playerDto.role)
                                )
                            }
                            Session(
                                sessionId = message.sessionId!!,
                                players = players,
                                hostPlayerId = players.find { it.role == com.app.findthebug.core.common.PlayerRole.HOST }?.name,
                                masterPlayerId = players.find { it.role == com.app.findthebug.core.common.PlayerRole.MASTER }?.name
                            ).also { currentSession.value = it }
                        } else {
                            null
                        }
                    }
                    else -> null
                }
            }
    }

    override fun observeMessages(): Flow<WebSocketMessage> {
        return webSocketService.messages
    }

    override fun connectWebSocket() {
        webSocketService.connect()
    }

    override fun disconnectWebSocket() {
        webSocketService.disconnect()
    }

    override fun isConnected(): Boolean {
        return webSocketService.isConnected()
    }

    override suspend fun leaveLobby() {
        val request = WebSocketMessage.LeaveLobbyRequest()
        webSocketService.sendMessage(request)
        disconnectWebSocket()
    }

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