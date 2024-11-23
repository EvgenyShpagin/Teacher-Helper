package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.model.error.GroupNumberError
import com.tusur.teacherhelper.domain.util.PROHIBITED_GROUP_CHARS_REGEX
import com.tusur.teacherhelper.domain.util.Result
import javax.inject.Inject

class ValidateGroupNumberUseCase @Inject constructor(
    private val doesGroupNumberAlreadyExists: DoesGroupNumberAlreadyExistUseCase
) {
    suspend operator fun invoke(groupNumber: String): Result<Unit, GroupNumberError> {
        val numberExists = doesGroupNumberAlreadyExists(groupNumber)
        return when {
            numberExists -> Result.Error(GroupNumberError.ALREADY_EXISTS)
            else -> if (groupNumber.isEmpty()) {
                Result.Error(GroupNumberError.EMPTY)
            } else if (PROHIBITED_GROUP_CHARS_REGEX.containsMatchIn(groupNumber)) {
                Result.Error(GroupNumberError.INCORRECT)
            } else {
                Result.Success(Unit)
            }
        }
    }

    suspend operator fun invoke(
        groupNumber: String,
        ignoredGroups: List<Group>
    ): Result<Unit, GroupNumberError> {
        val result = invoke(groupNumber)
        val resultAsError = result as? Result.Error ?: return result
        if (resultAsError.error == GroupNumberError.ALREADY_EXISTS) {
            if (ignoredGroups.find { it.number == groupNumber } != null) {
                return Result.Success(Unit)
            }
        }
        return result
    }
}