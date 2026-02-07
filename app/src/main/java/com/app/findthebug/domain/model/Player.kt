package com.app.findthebug.domain.model

import com.app.findthebug.core.common.PlayerRole

data class Player(
    val id: String? = null,
    val name: String,
    val role: PlayerRole= PlayerRole.PLAYER,
    val joinedAt: Long = System.currentTimeMillis()
)