package com.app.findthebug.data.remote.model.request

import com.google.gson.annotations.SerializedName

data class SaveNoteRequest(
    @SerializedName("type") val type: String = "SAVE_NOTE",
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("playerId") val playerId: String,
    @SerializedName("clueId") val clueId: String,
    @SerializedName("content") val content: String
)