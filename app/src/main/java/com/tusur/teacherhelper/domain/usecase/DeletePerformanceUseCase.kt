package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Datetime
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository

class DeletePerformanceUseCase(private val studentPerformanceRepository: StudentPerformanceRepository) {
    suspend operator fun invoke(topicId: Int, groupListIds: List<Int>, datetime: List<Datetime>) {
        studentPerformanceRepository.deletePerformance(topicId, groupListIds, datetime)
    }

    suspend operator fun invoke(topicId: Int, groupListIds: List<Int>, datetime: Datetime) {
        invoke(topicId, groupListIds, listOf(datetime))
    }
}