package com.app.findthebug.data.remote.model.request

import com.google.gson.annotations.SerializedName

data class SubmitSolutionRequest(
    @SerializedName("type") val type: String = "SUBMIT_SOLUTION",
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("answers") val answers: List<String>
)