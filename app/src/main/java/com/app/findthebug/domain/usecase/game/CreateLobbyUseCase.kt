package com.app.findthebug.domain.usecase.game

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Session
import com.app.findthebug.domain.repository.IGameRepository
import javax.inject.Inject

class CreateLobbyUseCase @Inject constructor(
    private val gameRepository: IGameRepository
) {
    suspend operator fun invoke(playerName: String): Result<Session> {
        return gameRepository.createLobby(playerName)
    }
}