package com.app.findthebug.domain.usecase.evidence

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Evidence
import com.app.findthebug.domain.repository.IEvidenceRepository
import javax.inject.Inject

class SaveEvidenceUseCase @Inject constructor(
    private val evidenceRepository: IEvidenceRepository
) {
    suspend operator fun invoke(evidence: Evidence): Result<Unit> {
        return evidenceRepository.saveEvidence(evidence)
    }
}