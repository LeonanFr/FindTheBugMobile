package com.app.findthebug.domain.usecase.game

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.repository.IGameRepository
import javax.inject.Inject

class SubmitSolutionUseCase @Inject constructor(
    private val gameRepository: IGameRepository
) {
    suspend operator fun invoke(sessionId: String, answers: List<String>): Result<Unit> {
        return gameRepository.submitSolution(sessionId, answers)
    }
}