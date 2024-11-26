package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private typealias TopicPerformance = Pair<Topic, Performance>

class GetSubjectStudentPerformanceUseCase @Inject constructor(
    private val studentPerformanceRepository: StudentPerformanceRepository,
    private val topicRepository: TopicRepository
) {
    suspend operator fun invoke(
        studentId: Int,
        subjectId: Int
    ): Flow<List<TopicPerformance>> {
        val subjectTopicIds = topicRepository.getIdsBySubject(
            subjectId = subjectId,
            withCancelled = false
        )
        return studentPerformanceRepository.getFinalPerformance(subjectTopicIds, studentId)
            .map { existingTopicPerformance ->
                existingTopicPerformance.withMissedPerformance(subjectTopicIds)
            }
    }

    private suspend fun List<TopicPerformance>.withMissedPerformance(
        topicIds: List<Int>
    ): List<TopicPerformance> {
        return topicIds.map { topicId ->
            val topicPerformance = find { (topic, _) -> topic.id == topicId }

            if (topicPerformance == null) {
                val topic = topicRepository.getById(topicId)!!
                topic to topic.type.createEmptyPerformance()
            } else {
                val (topic, performance) = topicPerformance
                topic to performance.replaceNullWithEmpty(topic.type)
            }
        }
    }

    private fun TopicType.createEmptyPerformance(): Performance {
        return Performance(
            grade = if (isGradeAcceptable) PerformanceItem.Grade(0) else null,
            progress = if (isProgressAcceptable) PerformanceItem.Progress(0f) else null,
            assessment = if (isAssessmentAcceptable) PerformanceItem.Assessment.FAIL else null,
            attendance = if (isAttendanceAcceptable) emptyList() else null
        )
    }

    private fun Performance.replaceNullWithEmpty(topicType: TopicType) = copy(
        grade = if (topicType.isGradeAcceptable && grade == null) {
            PerformanceItem.Grade(0)
        } else {
            grade
        },
        progress = if (topicType.isProgressAcceptable && progress == null) {
            PerformanceItem.Progress(0f)
        } else {
            progress
        },
        assessment = if (topicType.isAssessmentAcceptable && assessment == null) {
            PerformanceItem.Assessment.FAIL
        } else {
            assessment
        },
        attendance = if (topicType.isAttendanceAcceptable && attendance == null) {
            emptyList()
        } else {
            attendance
        }
    )
}