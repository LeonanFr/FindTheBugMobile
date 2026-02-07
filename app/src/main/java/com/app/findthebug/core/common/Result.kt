package com.app.findthebug.core.common

sealed class Result<out T>{
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading

    fun getOrNull(): T? = when (this){
        is Success -> data
        else -> null
    }

    fun getOrThrow(): T = when (this){
        is Success -> data
        is Error -> throw exception ?: RuntimeException(message)
        Loading -> throw IllegalStateException("Result is loading")
    }

    fun errorOrNull(): String? = when (this){
        is Error -> message
        else -> null
    }

}