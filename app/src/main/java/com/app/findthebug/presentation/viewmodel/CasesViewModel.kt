package com.app.findthebug.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.BugCase
import com.app.findthebug.domain.repository.ICaseRepository
import com.app.findthebug.presentation.scenario.DebugCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CasesViewModel @Inject constructor(
    private val caseRepository: ICaseRepository
) : ViewModel() {

    private val _cases = MutableStateFlow<List<DebugCase>>(emptyList())
    val cases: StateFlow<List<DebugCase>> = _cases.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadCases() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = caseRepository.getCases()) {
                is Result.Success -> {
                    _cases.value = result.data.map {
                        DebugCase(
                            id = it.id,
                            title = it.title,
                            subtitle = it.shortDescription.ifBlank { it.description.ifBlank { "Sem descrição" } }
                        )
                    }
                }
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    private val _selectedCase = MutableStateFlow<BugCase?>(null)
    val selectedCase: StateFlow<BugCase?> = _selectedCase.asStateFlow()

    fun loadCaseDetails(caseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = caseRepository.getCaseDetails(caseId)) {
                is Result.Success -> _selectedCase.value = result.data
                is Result.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }
}