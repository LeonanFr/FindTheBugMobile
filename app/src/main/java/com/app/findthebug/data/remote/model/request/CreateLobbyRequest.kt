package com.app.findthebug.data.remote.model.request

import com.google.gson.annotations.SerializedName

data class CreateLobbyRequest(
    @SerializedName("type") val type: String = "CREATE_LOBBY",
    @SerializedName("playerName") val playerName: String
)