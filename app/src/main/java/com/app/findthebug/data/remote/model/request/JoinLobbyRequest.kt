package com.app.findthebug.data.remote.model.request

import com.google.gson.annotations.SerializedName

data class JoinLobbyRequest(
    @SerializedName("type") val type: String = "JOIN_AS_PLAYER",
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("playerName") val playerName: String
)