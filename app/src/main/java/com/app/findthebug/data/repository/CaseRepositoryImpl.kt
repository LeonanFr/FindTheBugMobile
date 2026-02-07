package com.app.findthebug.data.repository

import com.app.findthebug.core.common.Result
import com.app.findthebug.data.remote.api.ApiClient
import com.app.findthebug.data.remote.model.response.CaseResponse
import com.app.findthebug.domain.model.BugCase
import com.app.findthebug.domain.repository.ICaseRepository
import javax.inject.Inject

class CaseRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient
) : ICaseRepository {

    override suspend fun getCases(): Result<List<BugCase>> {
        return try {
            val response = ApiClient.safeApiCall { apiClient.api.getCases() }
            when (response) {
                is Result.Success -> {
                    val cases = response.data.map { it.toDomain() }
                    Result.Success(cases)
                }
                is Result.Error -> response
                else -> Result.Error("Unknown error")
            }
        } catch (e: Exception) {
            Result.Error("Failed to get cases: ${e.message}", e)
        }
    }

    override suspend fun getCaseDetails(caseId: String): Result<BugCase> {
        return try {
            val response = ApiClient.safeApiCall { apiClient.api.getCaseDetails(caseId) }
            when (response) {
                is Result.Success -> Result.Success(response.data.toDomain())
                is Result.Error -> response
                else -> Result.Error("Unknown error")
            }
        } catch (e: Exception) {
            Result.Error("Failed to get case details: ${e.message}", e)
        }
    }

    private fun CaseResponse.toDomain(): BugCase {
        return BugCase(
            id = this.id,
            title = this.title,
            description = this.description,
            systemTopology = com.app.findthebug.domain.model.SystemTopology(
                modules = this.systemTopology.modules.map { com.app.findthebug.domain.model.ModuleNode(it.name) },
                functions = this.systemTopology.functions.map { com.app.findthebug.domain.model.FunctionNode(it.name, it.parentId) },
                connections = this.systemTopology.connections.map { com.app.findthebug.domain.model.ConnectionNode(it.id, it.from, it.to) }
            )
        )
    }
}