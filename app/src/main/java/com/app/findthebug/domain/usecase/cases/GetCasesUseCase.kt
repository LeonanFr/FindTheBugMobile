package com.app.findthebug.domain.usecase.cases

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.BugCase
import com.app.findthebug.domain.repository.ICaseRepository
import javax.inject.Inject

class GetCasesUseCase @Inject constructor(
    private val caseRepository: ICaseRepository
) {
    suspend operator fun invoke(): Result<List<BugCase>> {
        return caseRepository.getCases()
    }
}