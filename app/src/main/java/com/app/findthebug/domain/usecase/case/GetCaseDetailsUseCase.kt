package com.app.findthebug.domain.usecase.case

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.BugCase
import com.app.findthebug.domain.repository.ICaseRepository
import javax.inject.Inject

class GetCaseDetailsUseCase @Inject constructor(
    private val caseRepository: ICaseRepository
) {
    suspend operator fun invoke(caseId: String): Result<BugCase> {
        return caseRepository.getCaseDetails(caseId)
    }
}