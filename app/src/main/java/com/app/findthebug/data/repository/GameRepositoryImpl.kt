package com.app.findthebug.data.repository

import android.util.Log
import com.app.findthebug.core.common.GamePhase
import com.app.findthebug.core.common.PlayerRole
import com.app.findthebug.core.common.Result
import com.app.findthebug.data.remote.api.WebSocketService
import com.app.findthebug.data.remote.model.websocket.*
import com.app.findthebug.domain.model.*
import com.app.findthebug.domain.repository.IGameRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val webSocketService: WebSocketService
) : IGameRepository {

    private val _currentGameState = MutableStateFlow<GameState?>(null)
    private val _currentSession = MutableStateFlow<Session?>(null)
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        repositoryScope.launch {
            webSocketService.messages.collect { message ->
                try {
                    when (message) {
                        is WebSocketMessage.GameStateUpdateResponse -> {
                            _currentGameState.value = convertToGameState(message)
                        }
                        is WebSocketMessage.LobbyUpdateResponse -> {
                            val players = message.players.map { Player(name = it.name, role = PlayerRole.fromInt(it.role)) }
                            _currentSession.value = Session(
                                sessionId = message.sessionId,
                                players = players,
                                masterPlayerId = players.find { it.role == PlayerRole.MASTER }?.name,
                                canStart = message.canStart,
                                phase = GamePhase.fromInt(message.phase ?: 0)
                            )
                        }
                        is WebSocketMessage.LobbyDestroyedResponse -> {
                            _currentGameState.value = null
                            _currentSession.value = null
                        }
                        is WebSocketMessage.GameStartedResponse -> {
                            val current = _currentSession.value
                            if (current != null) {
                                _currentSession.value = current.copy(phase = GamePhase.INVESTIGATION)
                            }
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e("GameRepository", "Erro de processamento: ${e.message}")
                }
            }
        }
    }

    private suspend fun ensureConnected(): Boolean {
        if (webSocketService.isConnected()) return true
        webSocketService.connect()
        repeat(50) { if (webSocketService.isConnected()) return true; delay(200) }
        return false
    }

    override suspend fun createLobby(playerName: String): Result<Session> {
        ensureConnected()
        val request = WebSocketMessage.CreateLobbyRequest(playerName = playerName)
        return when (val result = webSocketService.sendMessageAndWaitForResponse<WebSocketMessage.LobbyCreatedResponse>(request)) {
            is Result.Success -> {
                val p = Player(name = result.data.playerName, role = PlayerRole.MASTER)
                val s = Session(
                    sessionId = result.data.sessionId,
                    players = listOf(p),
                    masterPlayerId = p.name,
                    phase = GamePhase.LOBBY
                )
                _currentSession.value = s
                Result.Success(s)
            }
            else -> Result.Error("Erro ao criar lobby")
        }
    }

    override suspend fun joinLobby(sessionId: String, playerName: String): Result<Session> {
        ensureConnected()
        val request = WebSocketMessage.JoinAsPlayerRequest(sessionId = sessionId, playerName = playerName)
        return when (val result = webSocketService.sendMessageAndWaitForResponse<WebSocketMessage.JoinedLobbyResponse>(request)) {
            is Result.Success -> {
                val response = result.data
                val myRole = PlayerRole.fromInt(response.role ?: 0)

                val existingSession = _currentSession.value
                val currentList = existingSession?.players ?: emptyList()

                val updatedPlayers = if (currentList.none { it.name.trim().equals(playerName.trim(), true) }) {
                    currentList + Player(name = playerName, role = myRole)
                } else {
                    currentList.map {
                        if(it.name.trim().equals(playerName.trim(), true)) it.copy(role = myRole) else it
                    }
                }

                val s = Session(
                    sessionId = response.sessionId,
                    players = updatedPlayers,
                    masterPlayerId = existingSession?.masterPlayerId,
                    phase = GamePhase.fromInt(response.phase ?: 1)
                )
                _currentSession.value = s
                Result.Success(s)
            }
            else -> Result.Error("Erro ao sincronizar sessão")
        }
    }

    override suspend fun getLobbyInfo(sessionId: String): Result<Session> {
        ensureConnected()
        val request = WebSocketMessage.GetLobbyInfoRequest(sessionId = sessionId)
        return when (val result = webSocketService.sendMessageAndWaitForResponse<WebSocketMessage.LobbyInfoResponse>(request)) {
            is Result.Success -> {
                val players = result.data.players?.map { Player(name = it.name, role = PlayerRole.fromInt(it.role)) } ?: emptyList()
                val s = Session(
                    sessionId = sessionId,
                    players = players,
                    masterPlayerId = players.find { it.role == PlayerRole.MASTER }?.name,
                    phase = GamePhase.fromInt(result.data.phase ?: 0)
                )
                _currentSession.value = s
                Result.Success(s)
            }
            else -> Result.Error("Lobby inexistente")
        }
    }

    override suspend fun saveNote(sessionId: String, playerId: String, clueId: String, content: String): Result<Unit> {
        ensureConnected()
        val request = WebSocketMessage.SaveNoteRequest(
            sessionId = sessionId,
            playerId = playerId,
            clueId = clueId,
            content = content
        )
        return if (webSocketService.sendMessage(request)) Result.Success(Unit) else Result.Error("Erro ao salvar")
    }

    override suspend fun submitSolution(sessionId: String, answers: List<String>): Result<Unit> {
        ensureConnected()
        val request = WebSocketMessage.SubmitSolutionRequest(sessionId = sessionId, answers = answers)
        return if (webSocketService.sendMessage(request)) Result.Success(Unit) else Result.Error("Falha na submissão")
    }

    override fun observeGameState(sessionId: String): Flow<GameState?> = _currentGameState.asStateFlow()
    override fun observeSession(sessionId: String): Flow<Session?> = _currentSession.asStateFlow()

    override suspend fun startGame(sessionId: String, playerName: String, caseId: String): Result<Unit> {
        ensureConnected()
        webSocketService.sendMessage(WebSocketMessage.StartGameRequest(sessionId = sessionId, playerName = playerName, caseId = caseId))
        return Result.Success(Unit)
    }

    override suspend fun executeAction(sessionId: String, playerId: String, actionType: Int, targetId: String): Result<GameState> {
        ensureConnected()
        val request = WebSocketMessage.GameActionRequest(sessionId = sessionId, playerId = playerId, actionType = actionType, targetId = targetId)
        return when (val result = webSocketService.sendMessageAndWaitForResponse<WebSocketMessage.GameStateUpdateResponse>(request)) {
            is Result.Success -> {
                val gs = convertToGameState(result.data)
                _currentGameState.value = gs
                Result.Success(gs)
            }
            else -> Result.Error("Erro na ação")
        }
    }

    override suspend fun validateSolution(sessionId: String, approved: Boolean): Result<Unit> {
        ensureConnected()
        val request = WebSocketMessage.ValidateSolutionRequest(sessionId = sessionId, approved = approved)
        return if (webSocketService.sendMessage(request)) Result.Success(Unit) else Result.Error("Erro ao validar")
    }

    override suspend fun leaveLobby(sessionId: String, playerName: String) {
        webSocketService.sendMessage(WebSocketMessage.LeaveLobbyRequest(sessionId = sessionId, playerName = playerName))
        _currentGameState.value = null
        _currentSession.value = null
    }

    override suspend fun leaveLobbyBySession(sessionId: String, playerName: String) = leaveLobby(sessionId, playerName)
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
                    playerNotes = dto.playerNotes ?: emptyMap()
                )
            }
        )
    }
}