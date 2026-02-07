package com.app.findthebug.domain.usecase.evidence

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.Evidence
import com.app.findthebug.domain.repository.IEvidenceRepository
import javax.inject.Inject

class GetEvidencesUseCase @Inject constructor(
    private val evidenceRepository: IEvidenceRepository
) {
    suspend operator fun invoke(sessionId: String): Result<List<Evidence>> {
        return evidenceRepository.getEvidences(sessionId)
    }
}