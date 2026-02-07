package com.app.findthebug.domain.usecase.session

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Session
import com.app.findthebug.domain.repository.ISessionRepository
import javax.inject.Inject

class SaveSessionUseCase @Inject constructor(
    private val sessionRepository: ISessionRepository
) {
    suspend operator fun invoke(session: Session): Result<Unit> {
        return sessionRepository.saveSession(session)
    }
}