package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository

class GetFinalPerformanceClassDayDatetimeMsUseCase(private val studentPerformanceRepository: StudentPerformanceRepository) {
    suspend operator fun invoke(studentId: Int, topicId: Int): Long? {
        return studentPerformanceRepository.getFinalPerformanceClassDayDatetimeMs(
            studentId,
            topicId
        )
    }
}
