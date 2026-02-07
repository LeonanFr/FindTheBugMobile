package com.app.findthebug.domain.usecase.game

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.GameState
import com.app.findthebug.domain.repository.IGameRepository
import javax.inject.Inject

class ExecuteActionUseCase @Inject constructor(
    private val gameRepository: IGameRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        playerId: String,
        actionType: Int,
        targetId: String
    ): Result<GameState> {
        return gameRepository.executeAction(sessionId, playerId, actionType, targetId)
    }
}