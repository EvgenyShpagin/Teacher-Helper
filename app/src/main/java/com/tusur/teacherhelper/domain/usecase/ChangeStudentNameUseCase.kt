package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.repository.StudentRepository

class ChangeStudentNameUseCase(private val studentRepository: StudentRepository) {
    suspend operator fun invoke(
        student: Student,
        groupId: Int,
        name: Student.Name
    ) {
        studentRepository.update(
            student.copy(name = name),
            groupId
        )
    }
}