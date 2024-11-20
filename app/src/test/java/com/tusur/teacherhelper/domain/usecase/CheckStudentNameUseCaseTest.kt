package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Result
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.error.StudentNameError
import com.tusur.teacherhelper.domain.repository.StudentRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CheckStudentNameUseCaseTest {

    private val studentRepository: StudentRepository = mockk()
    private val useCase = CheckStudentNameUseCase(studentRepository)

    private val validName = Student.Name("Иванов", "Иван", "Иванович")
    private val invalidName = Student.Name("Invalid", "N%me", "Иванович")
    private val existingName = Student.Name("Петр", "Петров", "")
    private val studentId = 1
    private val anotherStudentId = 2

    @Test
    fun shouldReturnNotChanged_whenStudentAlreadyHasThisName() = runTest {
        coEvery { studentRepository.getByName(validName) } returns Student(studentId, validName)
        assertEquals(
            Result.Error<Unit, StudentNameError>(StudentNameError.NOT_CHANGED),
            useCase(studentId, validName)
        )
    }

    @Test
    fun shouldReturnAlreadyExists_whenNameBelongsToAnotherStudent() = runTest {
        coEvery { studentRepository.getByName(existingName) } returns
                Student(anotherStudentId, existingName)
        assertEquals(
            Result.Error<Unit, StudentNameError>(StudentNameError.ALREADY_EXISTS),
            useCase(studentId, existingName)
        )
    }

    @Test
    fun shouldReturnIncorrect_whenNameFormatIsInvalid() = runTest {
        coEvery { studentRepository.getByName(invalidName) } returns null
        assertEquals(
            Result.Error<Unit, StudentNameError>(StudentNameError.INCORRECT),
            useCase(studentId, invalidName)
        )
    }

    @Test
    fun shouldReturnSuccess_whenNameIsValidAndUnique() = runTest {
        coEvery { studentRepository.getByName(validName) } returns null
        assertEquals(
            Result.Success<Unit, StudentNameError>(Unit),
            useCase(studentId, validName)
        )
    }
}