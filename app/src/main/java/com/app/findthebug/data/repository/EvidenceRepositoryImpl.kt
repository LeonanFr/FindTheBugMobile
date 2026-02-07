package com.app.findthebug.data.repository

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Evidence
import com.app.findthebug.domain.repository.IEvidenceRepository
import javax.inject.Inject

class EvidenceRepositoryImpl @Inject constructor() : IEvidenceRepository {

    private val evidences = mutableMapOf<String, MutableList<Evidence>>()

    override suspend fun saveEvidence(evidence: Evidence): Result<Unit> {
        return try {
            val sessionEvidences = evidences.getOrPut(evidence.clueId) { mutableListOf() }
            val existingIndex = sessionEvidences.indexOfFirst { it.id == evidence.id && it.playerId == evidence.playerId && it.clueId == evidence.clueId }

            if (existingIndex != -1) {
                sessionEvidences[existingIndex] = evidence.copy(updatedAt = System.currentTimeMillis())
            } else {
                sessionEvidences.add(evidence.copy(createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to save evidence: ${e.message}", e)
        }
    }

    override suspend fun getEvidences(sessionId: String): Result<List<Evidence>> {
        return try {
            val allEvidences = evidences.values.flatten()
            val sessionEvidences = allEvidences.filter { it.clueId.contains(sessionId) }
            Result.Success(sessionEvidences.sortedBy { it.createdAt })
        } catch (e: Exception) {
            Result.Error("Failed to get evidences: ${e.message}", e)
        }
    }

    override suspend fun deleteEvidence(evidenceId: String): Result<Unit> {
        return try {
            evidences.values.forEach { evList ->
                evList.removeIf { it.id == evidenceId }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete evidence: ${e.message}", e)
        }
    }
}