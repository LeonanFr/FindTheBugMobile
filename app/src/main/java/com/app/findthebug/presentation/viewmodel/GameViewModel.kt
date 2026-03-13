package com.app.findthebug.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.findthebug.core.common.PlayerRole
import com.app.findthebug.core.common.Result
import com.app.findthebug.core.datastore.SessionPreferences
import com.app.findthebug.domain.model.GameState
import com.app.findthebug.domain.model.Session
import com.app.findthebug.domain.usecase.game.*
import com.app.findthebug.domain.usecase.session.LoadSessionUseCase
import com.app.findthebug.domain.usecase.session.SaveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val sessionPreferences: SessionPreferences
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

    private var currentSessionId: String? = null

    init {
        viewModelScope.launch {
            sessionPreferences.playerName.collect { name ->
                _currentPlayerName.value = name
            }
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
                    sessionPreferences.saveSession(result.data.sessionId, playerName)
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
                    sessionPreferences.saveSession(sessionId, playerName)
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
        val sessionId = currentSessionId ?: return
        val playerName = _currentPlayerName.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = startGameUseCase(sessionId, playerName, caseId)) {
                is Result.Success -> {
                    sessionPreferences.saveSession(sessionId, playerName)
                }
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
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = submitSolutionUseCase(sessionId, answers)) {
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun saveNote(clueId: String, content: String) {
        val sessionId = currentSessionId ?: return
        val playerId = _currentPlayerName.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = saveNoteUseCase(sessionId, playerId, clueId, content)) {
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
        currentSessionId = null
        _currentPlayerName.value = null
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