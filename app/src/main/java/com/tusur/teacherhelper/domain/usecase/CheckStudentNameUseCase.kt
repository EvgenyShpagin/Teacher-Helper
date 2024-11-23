package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.error.StudentNameError
import com.tusur.teacherhelper.domain.repository.StudentRepository
import com.tusur.teacherhelper.domain.util.Result
import javax.inject.Inject

class CheckStudentNameUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(
        studentId: Int,
        name: Student.Name
    ): Result<Unit, StudentNameError> {
        val studentWithItName = studentRepository.getByName(name)
        return if (studentWithItName?.id == studentId) {
            Result.Error(StudentNameError.NOT_CHANGED)
        } else if (studentWithItName?.name == name) {
            Result.Error(StudentNameError.ALREADY_EXISTS)
        } else if (NAME_REGEX.matches(name.toString())) {
            Result.Success(Unit)
        } else {
            Result.Error(StudentNameError.INCORRECT)
        }
    }

    private companion object {
        val NAME_REGEX = "^[А-ЯA-Zа-яa-z]+(?:\\s[А-ЯA-Zа-яa-z]+){1,2}$".toRegex()
    }
}