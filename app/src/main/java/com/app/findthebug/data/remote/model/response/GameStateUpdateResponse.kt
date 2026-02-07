package com.app.findthebug.data.remote.model.response

import com.google.gson.annotations.SerializedName

data class GameStateUpdateResponse(
    @SerializedName("type") val type: String,
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("currentDay") val currentDay: Int,
    @SerializedName("remainingPoints") val remainingPoints: Int,
    @SerializedName("currentTurnIndex") val currentTurnIndex: Int? = null,
    @SerializedName("currentTurnPlayer") val currentTurnPlayer: String? = null,
    @SerializedName("discoveredClues") val discoveredClues: List<DiscoveredClueDto> = emptyList()
)

data class DiscoveredClueDto(
    @SerializedName("id") val id: String,
    @SerializedName("targetId") val targetId: String,
    @SerializedName("type") val type: Int,
    @SerializedName("content") val content: String,
    @SerializedName("playerNotes") val playerNotes: Map<String, String> = emptyMap()
)