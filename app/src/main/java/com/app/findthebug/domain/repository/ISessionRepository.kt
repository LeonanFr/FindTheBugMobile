package com.app.findthebug.domain.repository

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Session

interface ISessionRepository {
    suspend fun saveSession(session: Session): Result<Unit>
    suspend fun loadSession(sessionId: String): Result<Session?>
    suspend fun clearSession(sessionId: String): Result<Unit>
}