package com.app.findthebug.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.findthebug.core.common.Result
import com.app.findthebug.core.datastore.SessionPreferences
import com.app.findthebug.domain.repository.IGameRepository
import com.app.findthebug.domain.usecase.session.ValidateSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val sessionPreferences: SessionPreferences,
    private val gameRepository: IGameRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState

    sealed class SessionState {
        object Idle : SessionState()
        object Loading : SessionState()
        data class ValidSession(val sessionId: String, val playerName: String) : SessionState()
        object NoSession : SessionState()
    }

    fun checkStoredSession() {
        viewModelScope.launch {
            _sessionState.value = SessionState.Loading
            val sessionId = sessionPreferences.sessionId.first()
            val playerName = sessionPreferences.playerName.first()
            val active = sessionPreferences.isSessionActive.first()

            if (active && sessionId != null && playerName != null) {
                val isValid = when (val result = validateSessionUseCase(sessionId, playerName)) {
                    is Result.Success -> result.data
                    else -> false
                }
                if (isValid) {
                    _sessionState.value = SessionState.ValidSession(sessionId, playerName)
                } else {
                    sessionPreferences.clearSession()
                    _sessionState.value = SessionState.NoSession
                }
            } else {
                _sessionState.value = SessionState.NoSession
            }
        }
    }

    fun clearSession(sessionId: String, playerName: String) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                gameRepository.leaveLobby(sessionId, playerName)
                sessionPreferences.clearSession()
            }
            _sessionState.value = SessionState.NoSession
        }
    }
}