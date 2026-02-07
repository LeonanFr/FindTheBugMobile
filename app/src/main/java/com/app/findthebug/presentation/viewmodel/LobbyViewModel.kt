package com.app.findthebug.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Session
import com.app.findthebug.domain.usecase.game.CreateLobbyUseCase
import com.app.findthebug.domain.usecase.game.GetLobbyInfoUseCase
import com.app.findthebug.domain.usecase.game.JoinAsMasterUseCase
import com.app.findthebug.domain.usecase.game.JoinLobbyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val createLobbyUseCase: CreateLobbyUseCase,
    private val joinLobbyUseCase: JoinLobbyUseCase,
    private val joinAsMasterUseCase: JoinAsMasterUseCase,
    private val getLobbyInfoUseCase: GetLobbyInfoUseCase
) : ViewModel() {

    private val _currentSession = MutableStateFlow<Session?>(null)
    val currentSession: StateFlow<Session?> = _currentSession

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun createLobby(playerName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = createLobbyUseCase(playerName)) {
                is Result.Success -> {
                    _currentSession.value = result.data
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
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
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
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
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
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
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}