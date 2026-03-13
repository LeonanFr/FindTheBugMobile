package com.app.findthebug.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.findthebug.core.common.Result
import com.app.findthebug.core.datastore.SessionPreferences
import com.app.findthebug.domain.model.Session
import com.app.findthebug.domain.repository.IGameRepository
import com.app.findthebug.domain.usecase.game.CreateLobbyUseCase
import com.app.findthebug.domain.usecase.game.GetLobbyInfoUseCase
import com.app.findthebug.domain.usecase.game.JoinAsMasterUseCase
import com.app.findthebug.domain.usecase.game.JoinLobbyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val createLobbyUseCase: CreateLobbyUseCase,
    private val joinLobbyUseCase: JoinLobbyUseCase,
    private val joinAsMasterUseCase: JoinAsMasterUseCase,
    private val getLobbyInfoUseCase: GetLobbyInfoUseCase,
    private val gameRepository: IGameRepository,
    private val sessionPreferences: SessionPreferences
) : ViewModel() {

    private val _currentSession = MutableStateFlow<Session?>(null)
    val currentSession: StateFlow<Session?> = _currentSession

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentPlayerName = MutableStateFlow<String?>(null)
    val currentPlayerName: StateFlow<String?> = _currentPlayerName.asStateFlow()

    private val _showExitConfirmation = MutableStateFlow(false)
    val showExitConfirmation: StateFlow<Boolean> = _showExitConfirmation.asStateFlow()

    private val _hasLoadedOnce = MutableStateFlow(false)
    val hasLoadedOnce: StateFlow<Boolean> = _hasLoadedOnce.asStateFlow()

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
                    setCurrentPlayerName(playerName)
                    sessionPreferences.saveSession(result.data.sessionId, playerName)
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
                    setCurrentPlayerName(playerName)
                    sessionPreferences.saveSession(sessionId, playerName)
                }
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun joinAsMaster(sessionId: String, masterName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = joinAsMasterUseCase(sessionId, masterName)) {
                is Result.Success -> {
                    _currentSession.value = result.data
                    setCurrentPlayerName(masterName)
                    sessionPreferences.saveSession(sessionId, masterName)
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
                is Result.Success -> {
                    _currentSession.value = result.data
                    _hasLoadedOnce.value = true
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                    _hasLoadedOnce.value = true
                    if (result.message.contains("404") || result.message.contains("not found") || result.message.contains("não encontrado")) {
                        sessionPreferences.clearSession()
                        _currentSession.value = null
                        _currentPlayerName.value = null
                    }
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun startObservingSession(sessionId: String) {
        viewModelScope.launch {
            gameRepository.observeSession(sessionId).collect { session ->
                _currentSession.value = session
                if (session != null) _hasLoadedOnce.value = true
            }
        }
    }

    fun leaveLobby() {
        viewModelScope.launch {
            _isLoading.value = true
            gameRepository.leaveLobby()
            sessionPreferences.clearSession()
            _currentSession.value = null
            _currentPlayerName.value = null
            _isLoading.value = false
        }
    }

    fun confirmExit(confirm: Boolean) {
        if (confirm) {
            leaveLobby()
        }
        _showExitConfirmation.value = false
    }

    fun showExitDialog() {
        _showExitConfirmation.value = true
    }

    fun clearError() {
        _errorMessage.value = null
    }
}