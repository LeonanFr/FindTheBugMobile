package com.app.findthebug.domain.model

import com.app.findthebug.core.common.ClueType

data class Clue(
    val id: String,
    val targetId: String,
    val type: ClueType,
    val content: String,
    val discoveredBy: String? = null,
    val playerNotes: Map<String, String> = emptyMap()
)