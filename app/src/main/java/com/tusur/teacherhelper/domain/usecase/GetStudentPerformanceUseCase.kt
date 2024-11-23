package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.repository.ClassDateRepository
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import javax.inject.Inject

class GetStudentPerformanceUseCase @Inject constructor(
    private val studentPerformanceRepository: StudentPerformanceRepository,
    private val classDateRepository: ClassDateRepository
) {
    suspend operator fun invoke(studentId: Int, topicId: Int, datetimeMillis: Long): Performance {
        val classDateId = classDateRepository.getIdByMillis(datetimeMillis)
            ?: return emptyPerformance
        return studentPerformanceRepository.getSetPerformance(topicId, studentId, classDateId)
            ?: emptyPerformance
    }

    private companion object {
        val emptyPerformance = Performance(null, null, null, null)
    }
}