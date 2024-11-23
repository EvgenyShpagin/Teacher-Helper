package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.model.error.SubjectNameError
import com.tusur.teacherhelper.domain.repository.SubjectRepository
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.domain.util.withoutUnwantedSpaces
import javax.inject.Inject

class ValidateSubjectNameUseCase @Inject constructor(
    private val subjectRepository: SubjectRepository
) {
    suspend operator fun invoke(name: String): Result<Subject, SubjectNameError> {
        val trimmedName = name.withoutUnwantedSpaces()
        val subjectWithItName = subjectRepository.getByName(trimmedName)
        return when {
            subjectWithItName != null -> Result.Error(SubjectNameError.ALREADY_EXISTS)
            trimmedName.isEmpty() -> Result.Error(SubjectNameError.EMPTY)
            trimmedName.matches(NAME_REGEX) -> Result.Success(data = Subject(NO_ID, trimmedName))
            else -> Result.Error(SubjectNameError.INCORRECT)
        }
    }

    private companion object {
        val NAME_REGEX = "[А-ЯЁA-Zа-яёa-z]+(?:\\s[А-ЯЁA-Zа-яёa-z]+)*".toRegex()
    }
}