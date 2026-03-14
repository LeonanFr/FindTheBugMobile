package com.app.findthebug.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.findthebug.core.common.Result
import com.app.findthebug.core.datastore.SessionPreferences
import com.app.findthebug.data.remote.model.websocket.WebSocketMessage
import com.app.findthebug.domain.model.Session
import com.app.findthebug.domain.repository.IGameRepository
import com.app.findthebug.domain.usecase.game.CreateLobbyUseCase
import com.app.findthebug.domain.usecase.game.GetLobbyInfoUseCase
import com.app.findthebug.domain.usecase.game.JoinLobbyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val createLobbyUseCase: CreateLobbyUseCase,
    private val joinLobbyUseCase: JoinLobbyUseCase,
    private val getLobbyInfoUseCase: GetLobbyInfoUseCase,
    private val gameRepository: IGameRepository,
    private val sessionPreferences: SessionPreferences
) : ViewModel() {

    data class UiState(
        val session: Session? = null,
        val currentPlayerName: String? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val showExitConfirmation: Boolean = false,
        val showLobbyDestroyedDialog: Boolean = false,
        val hasLoadedOnce: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    sealed class NavigationEvent {
        object GoToHome : NavigationEvent()
        data class GoToInvestigation(val caseId: String) : NavigationEvent()
    }

    private var observeJob: Job? = null
    private var messagesJob: Job? = null

    init {
        viewModelScope.launch {
            sessionPreferences.playerName.collect { name ->
                _uiState.value = _uiState.value.copy(currentPlayerName = name)
            }
        }
        observeLobbyDestroyed()
    }

    private fun observeLobbyDestroyed() {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            gameRepository.observeMessages().collect { message ->
                when (message) {
                    is WebSocketMessage.LobbyDestroyedResponse -> {
                        if (_uiState.value.session != null) {
                            _uiState.value = _uiState.value.copy(
                                showLobbyDestroyedDialog = true
                            )
                        }
                    }
                    is WebSocketMessage.GameStartedResponse -> {
                        _navigationEvent.emit(NavigationEvent.GoToInvestigation(message.caseId))
                    }
                    is WebSocketMessage.ErrorResponse -> {
                        if (message.message.contains("not found") || message.message.contains("não encontrado")) {
                            clearLocalSession()
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun createLobby(playerName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                showLobbyDestroyedDialog = false
            )

            when (val result = createLobbyUseCase(playerName)) {
                is Result.Success -> {
                    val sessionId = result.data.sessionId
                    _uiState.value = _uiState.value.copy(
                        session = result.data,
                        currentPlayerName = playerName,
                        isLoading = false,
                        hasLoadedOnce = true
                    )
                    sessionPreferences.saveSession(sessionId, playerName)
                    startObservingSession(sessionId)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    suspend fun joinLobby(sessionId: String, playerName: String): Result<Session> {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            showLobbyDestroyedDialog = false
        )

        startObservingSession(sessionId)

        val result = joinLobbyUseCase(sessionId, playerName)

        when (result) {
            is Result.Success -> {
                _uiState.value = _uiState.value.copy(
                    session = result.data,
                    currentPlayerName = playerName,
                    isLoading = false,
                    hasLoadedOnce = true
                )
                sessionPreferences.saveSession(sessionId, playerName)
            }
            is Result.Error -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.message,
                    isLoading = false
                )
                if (result.message.contains("not found") || result.message.contains("não encontrado")) {
                    clearLocalSession()
                }
            }
            else -> {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
        return result
    }

    fun startObservingSession(sessionId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            gameRepository.observeSession(sessionId).collect { session ->
                Log.d("LobbyViewModel", "Session updated: players=${session?.players?.map { it.name }}")

                if (session != null) {
                    _uiState.value = _uiState.value.copy(
                        session = session,
                        hasLoadedOnce = true
                    )
                }
            }
        }
    }

    fun leaveLobby() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val sessionId = _uiState.value.session?.sessionId
            val playerName = _uiState.value.currentPlayerName

            if (sessionId != null && playerName != null) {
                gameRepository.leaveLobby(sessionId, playerName)
            }

            clearLocalSession()
        }
    }

    private fun clearLocalSession() {
        viewModelScope.launch {
            observeJob?.cancel()
            sessionPreferences.clearSession()
            _uiState.value = UiState()
            _navigationEvent.emit(NavigationEvent.GoToHome)
        }
    }

    fun confirmExit(confirm: Boolean) {
        if (confirm) {
            leaveLobby()
        } else {
            _uiState.value = _uiState.value.copy(showExitConfirmation = false)
        }
    }

    fun showExitDialog() {
        _uiState.value = _uiState.value.copy(showExitConfirmation = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearLobbyDestroyedDialog() {
        _uiState.value = _uiState.value.copy(showLobbyDestroyedDialog = false)
        clearLocalSession()
    }
}