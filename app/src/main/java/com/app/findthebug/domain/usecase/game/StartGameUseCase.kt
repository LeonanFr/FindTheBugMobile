package com.app.findthebug.domain.usecase.game

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.repository.IGameRepository
import javax.inject.Inject

class StartGameUseCase @Inject constructor(
    private val gameRepository: IGameRepository
) {
    suspend operator fun invoke(sessionId: String, playerName: String, caseId: String): Result<Unit> {
        return gameRepository.startGame(sessionId, playerName, caseId)
    }
}