package com.app.findthebug.domain.model

data class Session(
    val sessionId: String,
    val players: List<Player>,
    val hostPlayerId: String? = null,
    val masterPlayerId: String? = null,
    val canStart: Boolean = false,
    val caseId: String? = null
)