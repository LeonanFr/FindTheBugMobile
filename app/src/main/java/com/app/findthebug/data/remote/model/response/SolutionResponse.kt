package com.app.findthebug.data.remote.model.response

import com.google.gson.annotations.SerializedName

data class SolutionResponse(
    @SerializedName("type") val type: String,
    @SerializedName("sessionId") val sessionId: String? = null,
    @SerializedName("teamAnswers") val teamAnswers: List<String>? = null,
    @SerializedName("questions") val questions: List<String>? = null,
    @SerializedName("correctAnswers") val correctAnswers: List<String>? = null,
    @SerializedName("message") val message: String? = null
)