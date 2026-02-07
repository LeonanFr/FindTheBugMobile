package com.app.findthebug.data.remote.model.response

import com.google.gson.annotations.SerializedName

data class ClueRevealedResponse(
    @SerializedName("type") val type: String,
    @SerializedName("clueId") val clueId: String,
    @SerializedName("content") val content: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("investigator") val investigator: String
)