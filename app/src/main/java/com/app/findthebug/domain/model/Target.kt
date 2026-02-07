package com.app.findthebug.domain.model

import com.app.findthebug.core.common.TargetType

data class Target(
    val id: String,
    val name: String,
    val type: TargetType,
    val parentId: String? = null
)