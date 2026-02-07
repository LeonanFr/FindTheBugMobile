package com.app.findthebug.domain.usecase.game

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Session
import com.app.findthebug.domain.repository.IGameRepository
import javax.inject.Inject

class JoinAsMasterUseCase @Inject constructor(
    private val gameRepository: IGameRepository
) {
    suspend operator fun invoke(sessionId: String, masterName: String): Result<Session> {
        return gameRepository.joinAsMaster(sessionId, masterName)
    }
}