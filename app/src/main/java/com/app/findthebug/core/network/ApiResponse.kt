package com.app.findthebug.core.network

import com.app.findthebug.core.common.Result

sealed class ApiResponse<T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error<T>(val message: String, val code: Int? = null) : ApiResponse<T>()

    fun toResult(): Result<T> = when (this) {
        is Success -> Result.Success(data)
        is Error -> Result.Error(message)
    }
}