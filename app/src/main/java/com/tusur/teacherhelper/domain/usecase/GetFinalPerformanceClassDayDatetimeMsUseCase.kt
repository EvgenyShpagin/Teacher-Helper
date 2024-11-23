package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import javax.inject.Inject

class GetFinalPerformanceClassDayDatetimeMsUseCase @Inject constructor(
    private val studentPerformanceRepository: StudentPerformanceRepository
) {
    suspend operator fun invoke(studentId: Int, topicId: Int): Long? {
        return studentPerformanceRepository.getFinalPerformanceClassDayDatetimeMs(
            studentId,
            topicId
        )
    }
}
