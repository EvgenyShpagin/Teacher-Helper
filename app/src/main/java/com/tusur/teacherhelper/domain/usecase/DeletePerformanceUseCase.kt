package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class DeletePerformanceUseCase @Inject constructor(
    private val studentPerformanceRepository: StudentPerformanceRepository
) {
    suspend operator fun invoke(
        topicId: Int,
        groupListIds: List<Int>,
        datetime: List<LocalDateTime>
    ) {
        studentPerformanceRepository.deletePerformance(topicId, groupListIds, datetime)
    }

    suspend operator fun invoke(topicId: Int, groupListIds: List<Int>, datetime: LocalDateTime) {
        invoke(topicId, groupListIds, listOf(datetime))
    }
}