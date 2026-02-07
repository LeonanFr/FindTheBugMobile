package com.app.findthebug.domain.model

data class Solution(
    val answers: List<String>,
    val submittedBy: String,
    val submittedAt: Long = System.currentTimeMillis(),
    val isCorrect: Boolean? = null,
    val feedback: String? = null
)