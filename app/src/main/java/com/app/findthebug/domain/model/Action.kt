package com.app.findthebug.domain.model

import com.app.findthebug.core.common.ActionType

data class Action(
    val type: ActionType,
    val targetId: String,
    val playerId: String,
    val timestamp: Long = System.currentTimeMillis()
)