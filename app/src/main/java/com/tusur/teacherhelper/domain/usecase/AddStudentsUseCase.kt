package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.repository.StudentRepository
import javax.inject.Inject

class AddStudentsUseCase @Inject constructor(private val studentRepository: StudentRepository) {
    suspend operator fun invoke(students: List<Student>, groupId: Int) {
        studentRepository.add(students, groupId)
    }
}