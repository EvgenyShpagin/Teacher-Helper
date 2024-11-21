package com.tusur.teacherhelper.domain.model

import com.tusur.teacherhelper.domain.model.error.Error

typealias RootError = Error

sealed class Result<out D, out E : RootError> {
    data class Success<out D, out E : RootError>(val data: D) : Result<D, E>()
    data class Error<out D, out E : RootError>(val error: E) : Result<D, E>()

    inline fun onSuccess(action: (data: D) -> Unit): Result<D, E> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    inline fun onFailure(action: (error: E) -> Unit): Result<D, E> {
        if (this is Result.Error) {
            action(error)
        }
        return this
    }

    companion object
}

/**
 * To simplify call when D is Unit ('Result.Success()' instead of 'Result.Success(Unit)')
 */
fun <E : RootError> Result.Companion.Success() = Result.Success<Unit, E>(Unit)