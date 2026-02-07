package com.app.findthebug.data.remote.api

import com.app.findthebug.data.remote.model.response.CaseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FindTheBugApi {
    @GET(ApiEndpoints.CASES)
    suspend fun getCases(): Response<List<CaseResponse>>

    @GET(ApiEndpoints.CASE_DETAILS)
    suspend fun getCaseDetails(@Path("caseId") caseId: String): Response<CaseResponse>
}