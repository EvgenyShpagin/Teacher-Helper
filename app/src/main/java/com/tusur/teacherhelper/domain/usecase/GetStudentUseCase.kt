package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.repository.StudentRepository
import javax.inject.Inject

class GetStudentUseCase @Inject constructor(private val studentRepository: StudentRepository) {
    suspend operator fun invoke(studentId: Int): Student {
        return studentRepository.getById(studentId)!!
    }
}