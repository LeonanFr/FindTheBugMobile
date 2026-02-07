package com.app.findthebug.data.remote.model.response

import com.google.gson.annotations.SerializedName

data class LobbyCreatedResponse(
    @SerializedName("type") val type: String,
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("playerName") val playerName: String,
    @SerializedName("role") val role: Int
)