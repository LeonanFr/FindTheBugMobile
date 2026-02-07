package com.app.findthebug.domain.usecase.game

import com.app.findthebug.domain.model.GameState
import com.app.findthebug.domain.repository.IGameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGameStateUseCase @Inject constructor(
    private val gameRepository: IGameRepository
) {
    operator fun invoke(sessionId: String): Flow<GameState?> {
        return gameRepository.observeGameState(sessionId)
    }
}