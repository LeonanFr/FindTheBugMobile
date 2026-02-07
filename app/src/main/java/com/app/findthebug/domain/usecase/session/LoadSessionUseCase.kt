package com.app.findthebug.domain.usecase.session

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Session
import com.app.findthebug.domain.repository.ISessionRepository
import javax.inject.Inject

class LoadSessionUseCase @Inject constructor(
    private val sessionRepository: ISessionRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Session?> {
        return sessionRepository.loadSession(sessionId)
    }
}