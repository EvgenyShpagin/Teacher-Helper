package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.StudentRepository

class DeleteStudentUseCase(private val studentRepository: StudentRepository) {
    suspend operator fun invoke(studentId: Int) {
        studentRepository.delete(studentId)
    }
}
