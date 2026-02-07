package com.app.findthebug.data.repository

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Session
import com.app.findthebug.domain.repository.ISessionRepository
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor() : ISessionRepository {

    private val storedSessions = mutableMapOf<String, Session>()

    override suspend fun saveSession(session: Session): Result<Unit> {
        return try {
            storedSessions[session.sessionId] = session
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to save session: ${e.message}", e)
        }
    }

    override suspend fun loadSession(sessionId: String): Result<Session?> {
        return try {
            val session = storedSessions[sessionId]
            Result.Success(session)
        } catch (e: Exception) {
            Result.Error("Failed to load session: ${e.message}", e)
        }
    }

    override suspend fun clearSession(sessionId: String): Result<Unit> {
        return try {
            storedSessions.remove(sessionId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to clear session: ${e.message}", e)
        }
    }
}