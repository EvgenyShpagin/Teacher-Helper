package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.repository.StudentRepository
import javax.inject.Inject

class GetPrevStudentUseCase @Inject constructor(private val studentRepository: StudentRepository) {
    suspend operator fun invoke(currentStudentId: Int, allStudentIds: List<Int>): Student? {
        val currentIndex = allStudentIds.indexOfLast { it == currentStudentId }
        return if (currentIndex == 0) {
            null
        } else {
            return studentRepository.getById(allStudentIds[currentIndex - 1])
        }
    }
}