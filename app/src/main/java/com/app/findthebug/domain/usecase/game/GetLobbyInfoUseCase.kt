package com.app.findthebug.domain.usecase.game

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Session
import com.app.findthebug.domain.repository.IGameRepository
import javax.inject.Inject

class GetLobbyInfoUseCase @Inject constructor(
    private val gameRepository: IGameRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Session> {
        return gameRepository.getLobbyInfo(sessionId)
    }
}