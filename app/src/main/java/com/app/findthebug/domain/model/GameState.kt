package com.app.findthebug.domain.model

import com.app.findthebug.core.common.Constants

data class GameState(
    val sessionId: String,
    val currentDay: Int = 1,
    val remainingPoints: Int = Constants.MAX_PF_PER_DAY,
    val isCompleted: Boolean = false,
    val isSuddenDeath: Boolean = false,
    val currentTurnPlayer: String? = null,
    val case: BugCase? = null,
    val players: List<Player> = emptyList(),
    val discoveredClues: List<Clue> = emptyList(),
    val investigatedTargets: Set<String> = emptySet(),
    val breakpointedTargets: Set<String> = emptySet()
){
    companion object{
        const val MAX_DAYS = Constants.MAX_DAYS
        const val MAX_PF_PER_DAY = Constants.MAX_PF_PER_DAY
    }
}