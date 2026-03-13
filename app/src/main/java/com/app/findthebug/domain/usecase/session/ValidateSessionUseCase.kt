package com.app.findthebug.domain.usecase.session

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.repository.IGameRepository
import javax.inject.Inject

class ValidateSessionUseCase @Inject constructor(
    private val gameRepository: IGameRepository
) {
    suspend operator fun invoke(sessionId: String, playerName: String): Result<Boolean> {
        return try {
            when (val result = gameRepository.getLobbyInfo(sessionId)) {
                is Result.Success -> {
                    val session = result.data
                    val playerExists = session.players.any { it.name == playerName }
                    Result.Success(playerExists)
                }
                is Result.Error -> Result.Success(false)
                else -> Result.Success(false)
            }
        } catch (e: Exception) {
            Result.Error("Erro ao validar sessão: ${e.message}")
        }
    }
}