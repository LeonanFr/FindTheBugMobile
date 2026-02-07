package com.app.findthebug.domain.usecase.game

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.repository.IGameRepository
import javax.inject.Inject

class ValidateSolutionUseCase @Inject constructor(
    private val gameRepository: IGameRepository
) {
    suspend operator fun invoke(sessionId: String, approved: Boolean): Result<Unit> {
        return gameRepository.validateSolution(sessionId, approved)
    }
}