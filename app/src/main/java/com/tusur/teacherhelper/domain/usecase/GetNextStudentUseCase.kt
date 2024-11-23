package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.repository.StudentRepository
import javax.inject.Inject

class GetNextStudentUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(currentStudentId: Int, allStudentIds: List<Int>): Student? {
        val currentIndex = allStudentIds.indexOfLast { it == currentStudentId }
        return if (currentIndex == allStudentIds.lastIndex) {
            null
        } else {
            return studentRepository.getById(allStudentIds[currentIndex + 1])
        }
    }
}