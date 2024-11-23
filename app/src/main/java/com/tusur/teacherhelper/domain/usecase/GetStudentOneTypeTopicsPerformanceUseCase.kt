package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.Topic
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetStudentOneTypeTopicsPerformanceUseCase @Inject constructor(
    private val getTotalStudentPerformance: GetSubjectStudentPerformanceUseCase
) {
    suspend operator fun invoke(
        studentId: Int,
        subjectId: Int,
        topicTypeId: Int
    ): List<Pair<Topic, Performance>> {
        return getTotalStudentPerformance(
            studentId,
            subjectId
        ).first().filter { it.first.type.id == topicTypeId }
    }
}