package com.app.findthebug.domain.usecase.game

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.repository.IGameRepository
import javax.inject.Inject

class SaveNoteUseCase @Inject constructor(
    private val gameRepository: IGameRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        playerId: String,
        clueId: String,
        content: String
    ): Result<Unit> {
        return gameRepository.saveNote(sessionId, playerId, clueId, content)
    }
}