package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.repository.StudentRepository
import com.tusur.teacherhelper.domain.util.NO_ID

class AddStudentUseCase(private val studentRepository: StudentRepository) {

    suspend operator fun invoke(student: Student, groupId: Int): Int {
        return studentRepository.add(student, groupId)
    }

    suspend operator fun invoke(studentName: Student.Name, groupId: Int): Int {
        return invoke(Student(NO_ID, studentName), groupId)
    }
}
