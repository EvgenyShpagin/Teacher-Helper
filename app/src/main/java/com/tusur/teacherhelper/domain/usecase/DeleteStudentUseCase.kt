package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.StudentRepository
import javax.inject.Inject

class DeleteStudentUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(studentId: Int) {
        studentRepository.delete(studentId)
    }
}
