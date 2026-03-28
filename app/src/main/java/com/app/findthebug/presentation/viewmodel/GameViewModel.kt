package com.app.findthebug.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.findthebug.core.common.PlayerRole
import com.app.findthebug.core.common.Result
import com.app.findthebug.core.common.ActionType
import com.app.findthebug.core.datastore.SessionPreferences
import com.app.findthebug.data.remote.api.WebSocketService
import com.app.findthebug.data.remote.model.websocket.WebSocketMessage
import com.app.findthebug.domain.model.GameState
import com.app.findthebug.domain.model.Session
import com.app.findthebug.domain.repository.IGameRepository
import com.app.findthebug.domain.usecase.game.*
import com.app.findthebug.domain.usecase.session.LoadSessionUseCase
import com.app.findthebug.domain.usecase.session.SaveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val createLobbyUseCase: CreateLobbyUseCase,
    private val joinLobbyUseCase: JoinLobbyUseCase,
    private val getLobbyInfoUseCase: GetLobbyInfoUseCase,
    private val startGameUseCase: StartGameUseCase,
    private val executeActionUseCase: ExecuteActionUseCase,
    private val submitSolutionUseCase: SubmitSolutionUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val validateSolutionUseCase: ValidateSolutionUseCase,
    private val getGameStateUseCase: GetGameStateUseCase,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val loadSessionUseCase: LoadSessionUseCase,
    private val sessionPreferences: SessionPreferences,
    private val webSocketService: WebSocketService,
    private val gameRepository: IGameRepository
) : ViewModel() {

    private val _currentSession = MutableStateFlow<Session?>(null)
    val currentSession: StateFlow<Session?> = _currentSession.asStateFlow()

    private val _currentGameState = MutableStateFlow<GameState?>(null)
    val currentGameState: StateFlow<GameState?> = _currentGameState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentPlayerName = MutableStateFlow<String?>(null)
    val currentPlayerName: StateFlow<String?> = _currentPlayerName.asStateFlow()

    private val _showSessionEndedDialog = MutableStateFlow(false)
    val showSessionEndedDialog: StateFlow<Boolean> = _showSessionEndedDialog.asStateFlow()

    private val _isWaitingForReview = MutableStateFlow(false)
    val isWaitingForReview: StateFlow<Boolean> = _isWaitingForReview.asStateFlow()

    data class RevealedClueInfo(
        val clueId: String,
        val content: String,
        val duration: Int
    )
    private val _revealedClue = MutableSharedFlow<RevealedClueInfo>()
    val revealedClue: SharedFlow<RevealedClueInfo> = _revealedClue.asSharedFlow()

    private val _turnSkipped = MutableSharedFlow<WebSocketMessage.TurnSkippedResponse>()
    val turnSkipped: SharedFlow<WebSocketMessage.TurnSkippedResponse> = _turnSkipped.asSharedFlow()

    private val _gameStarted = MutableSharedFlow<String>()
    val gameStarted: SharedFlow<String> = _gameStarted.asSharedFlow()

    private val _solutionForReview = MutableStateFlow<WebSocketMessage.SolutionForReviewResponse?>(null)
    val solutionForReview: StateFlow<WebSocketMessage.SolutionForReviewResponse?> = _solutionForReview.asStateFlow()

    private val _gameVictory = MutableSharedFlow<Unit>()
    val gameVictory: SharedFlow<Unit> = _gameVictory.asSharedFlow()

    private val _gameOver = MutableSharedFlow<Unit>()
    val gameOver: SharedFlow<Unit> = _gameOver.asSharedFlow()

    private val _solutionRejected = MutableSharedFlow<String>()
    val solutionRejected: SharedFlow<String> = _solutionRejected.asSharedFlow()

    private val _turnSkippedByMaster = MutableSharedFlow<String>()
    val turnSkippedByMaster: SharedFlow<String> = _turnSkippedByMaster.asSharedFlow()

    private var currentSessionId: String? = null

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    sealed class NavigationEvent {
        data class GoToInvestigation(val caseId: String) : NavigationEvent()
        object GoToHome : NavigationEvent()
        object GoToVictory : NavigationEvent()
        object GoToGameOver : NavigationEvent()
    }

    init {

        viewModelScope.launch {
            sessionPreferences.playerName.collect { name ->
                _currentPlayerName.value = name
            }
        }
        viewModelScope.launch {
            sessionPreferences.sessionId.collect { id ->
                currentSessionId = id
                if (id != null) {
                    getLobbyInfo(id)
                    startObservingGameState(id)
                }
            }
        }
        viewModelScope.launch {
            gameRepository.observeMessages().collect { message ->
                when (message) {
                    is WebSocketMessage.TurnSkippedByMasterResponse -> {
                        _turnSkippedByMaster.emit(message.previousPlayer)
                    }
                    is WebSocketMessage.GameStartedResponse -> {
                        _gameStarted.emit(message.caseId)
                        _navigationEvent.emit(NavigationEvent.GoToInvestigation(message.caseId))
                        startObservingGameState(message.sessionId)
                    }
                    is WebSocketMessage.SolutionForReviewResponse -> {
                        _isWaitingForReview.value = true
                        _solutionForReview.value = message
                    }
                    is WebSocketMessage.SolutionRejectedResponse -> {
                        _isWaitingForReview.value = false
                        _solutionRejected.emit(message.message)
                    }
                    is WebSocketMessage.LobbyDestroyedResponse -> {
                        sessionPreferences.clearSession()
                        gameRepository.disconnectWebSocket()
                        _showSessionEndedDialog.value = true
                        _navigationEvent.emit(NavigationEvent.GoToHome)
                    }
                    is WebSocketMessage.TurnSkippedResponse ->{
                        _turnSkipped.emit(message)
                    }
                    is WebSocketMessage.ClueRevealedResponse -> {
                        _revealedClue.emit(RevealedClueInfo(message.clueId, message.content, message.duration))
                    }
                    is WebSocketMessage.GameVictoryResponse -> {
                        _isWaitingForReview.value = false
                        _navigationEvent.emit(NavigationEvent.GoToVictory)
                    }
                    is WebSocketMessage.GameOverResponse -> {
                        _isWaitingForReview.value = false
                        _navigationEvent.emit(NavigationEvent.GoToGameOver)
                    }
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            webSocketService.connectionState.collect { state ->
                if (state == WebSocketService.ConnectionState.CONNECTED) {
                    val sid = currentSessionId
                    val name = _currentPlayerName.value
                    val role = sessionPreferences.role.first()
                    if (sid != null && name != null && role != null) {
                        val message = if (role == PlayerRole.MASTER.value) {
                            WebSocketMessage.JoinAsMasterRequest(sessionId = sid, masterName = name)
                        } else {
                            WebSocketMessage.JoinAsPlayerRequest(sessionId = sid, playerName = name)
                        }
                        webSocketService.sendMessage(message)
                    }
                }
            }
        }

    }

    fun resetWebSocket() {
        gameRepository.resetWebSocket()
    }

    fun skipTurn() {
        val sessionId = currentSessionId ?: return
        val playerId = _currentPlayerName.value ?: return
        viewModelScope.launch {
            gameRepository.executeAction(sessionId, playerId, ActionType.SKIP_TURN.value, "global")
        }
    }

    fun leaveGame() {
        viewModelScope.launch {
            val sid = currentSessionId
            val name = _currentPlayerName.value
            if (sid != null && name != null) {
                try {
                    gameRepository.leaveLobby(sid, name)

                    kotlinx.coroutines.delay(300)
                } catch (e: Exception) {
                }
            }

            gameRepository.disconnectWebSocket()
            sessionPreferences.clearSession()
            _navigationEvent.emit(NavigationEvent.GoToHome)
        }
    }

    fun dismissSessionEnded() {
        _showSessionEndedDialog.value = false
        viewModelScope.launch {
            sessionPreferences.clearSession()
            _navigationEvent.emit(NavigationEvent.GoToHome)
        }
    }

    fun setCurrentPlayerName(name: String) {
        _currentPlayerName.value = name
    }

    fun createLobby(playerName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = createLobbyUseCase(playerName)) {
                is Result.Success -> {
                    _currentSession.value = result.data
                    currentSessionId = result.data.sessionId
                    setCurrentPlayerName(playerName)
                    sessionPreferences.saveSession(result.data.sessionId, playerName, role = PlayerRole.MASTER.value)
                    startObservingGameState(result.data.sessionId)
                }
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun joinLobby(sessionId: String, playerName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = joinLobbyUseCase(sessionId, playerName)) {
                is Result.Success -> {
                    _currentSession.value = result.data
                    currentSessionId = sessionId
                    setCurrentPlayerName(playerName)
                    sessionPreferences.saveSession(sessionId, playerName, PlayerRole.PLAYER.value)
                    startObservingGameState(sessionId)
                }
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun getLobbyInfo(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = getLobbyInfoUseCase(sessionId)) {
                is Result.Success -> _currentSession.value = result.data
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun startGame(caseId: String = "case_robotics_001") {
        val sessionId = currentSessionId
        val playerName = _currentPlayerName.value

        if (sessionId == null || playerName == null) {
            _errorMessage.value = "Erro interno: Sessão não encontrada no ambiente."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = startGameUseCase(sessionId, playerName, caseId)) {
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun executeAction(actionType: Int, targetId: String) {
        val sessionId = currentSessionId ?: return
        val playerId = _currentPlayerName.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = executeActionUseCase(sessionId, playerId, actionType, targetId)) {
                is Result.Success -> _currentGameState.value = result.data
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun submitSolution(answers: List<String>) {
        val sessionId = currentSessionId ?: return

        viewModelScope.launch {
            _isWaitingForReview.value = true
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = submitSolutionUseCase(sessionId, answers)) {
                is Result.Error -> {
                    _isWaitingForReview.value = false
                    _errorMessage.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun masterSkipTurn() {
        val sid = currentSessionId ?: return
        val message = WebSocketMessage.MasterSkipTurnRequest(sessionId = sid)
        webSocketService.sendMessage(message)
    }

    fun saveNote(clueId: String, content: String) {
        val sessionId = currentSessionId ?: return
        val playerId = _currentPlayerName.value ?: return

        val finalContent = content.ifBlank {
            "[Nenhum observação anotada]"
        }

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = saveNoteUseCase(sessionId, playerId, clueId, finalContent)) {
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun validateSolution(approved: Boolean) {
        val sessionId = currentSessionId ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = validateSolutionUseCase(sessionId, approved)) {
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }

            _solutionForReview.value = null
            _isWaitingForReview.value = false
            _isLoading.value = false
        }
    }

    private fun startObservingGameState(sessionId: String) {
        viewModelScope.launch {
            getGameStateUseCase(sessionId).collect { gameState ->
                _currentGameState.value = gameState
            }
        }
    }

    fun saveCurrentSession() {
        viewModelScope.launch {
            _currentSession.value?.let { session ->
                when (val result = saveSessionUseCase(session)) {
                    is Result.Error -> _errorMessage.value = result.message
                    else -> {}
                }
            }
        }
    }

    fun loadSavedSession(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = loadSessionUseCase(sessionId)) {
                is Result.Success -> {
                    result.data?.let { session ->
                        _currentSession.value = session
                        currentSessionId = session.sessionId
                        startObservingGameState(session.sessionId)
                    }
                }
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetState() {
        _currentSession.value = null
        _currentGameState.value = null
        _errorMessage.value = null
        _isWaitingForReview.value = false
        _solutionForReview.value = null
    }

    fun canStartGame(): Boolean {
        return _currentSession.value?.canStart == true
    }

    fun isCurrentPlayerTurn(): Boolean {
        val currentPlayer = _currentPlayerName.value
        val currentTurnPlayer = _currentGameState.value?.currentTurnPlayer
        return currentPlayer != null && currentPlayer == currentTurnPlayer
    }

    fun hasRequiredPlayers(): Boolean {
        val players = _currentSession.value?.players ?: emptyList()
        val hasMaster = players.any { it.role == PlayerRole.MASTER }
        val hasPlayers = players.any { it.role == PlayerRole.PLAYER }
        return hasMaster && hasPlayers && players.size >= 2
    }
}