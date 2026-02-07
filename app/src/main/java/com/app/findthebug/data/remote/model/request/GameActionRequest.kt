package com.app.findthebug.data.remote.model.request

import com.google.gson.annotations.SerializedName

data class GameActionRequest(
    @SerializedName("type") val type: String = "GAME_ACTION",
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("playerId") val playerId: String,
    @SerializedName("actionType") val actionType: Int,
    @SerializedName("targetId") val targetId: String
)