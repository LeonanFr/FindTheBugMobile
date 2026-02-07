package com.app.findthebug.domain.repository

import com.app.findthebug.core.common.Result
import com.app.findthebug.domain.model.BugCase

interface ICaseRepository {
    suspend fun getCases(): Result<List<BugCase>>
    suspend fun getCaseDetails(caseId: String): Result<BugCase>
}