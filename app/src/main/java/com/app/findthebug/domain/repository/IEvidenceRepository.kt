package com.app.findthebug.domain.repository

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Evidence

interface IEvidenceRepository {
    suspend fun saveEvidence(evidence: Evidence): Result<Unit>
    suspend fun getEvidences(sessionId: String): Result<List<Evidence>>
    suspend fun deleteEvidence(evidenceId: String): Result<Unit>
}