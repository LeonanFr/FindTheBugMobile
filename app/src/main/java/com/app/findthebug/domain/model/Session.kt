package com.app.findthebug.domain.model

import com.app.findthebug.core.common.GamePhase

data class Session(
    val sessionId: String,
    val players: List<Player>,
    val masterPlayerId: String? = null,
    val canStart: Boolean = false,
    val caseId: String? = null,
    val phase: GamePhase = GamePhase.LOBBY
)