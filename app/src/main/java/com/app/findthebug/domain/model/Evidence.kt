package com.app.findthebug.domain.model

data class Evidence(
    val id: String,
    val clueId: String,
    val playerId: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)