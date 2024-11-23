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

class GetSubjectStudentPerformanceUseCase @Inject constructor(
    private val studentPerformanceRepository: StudentPerformanceRepository,
    private val topicRepository: TopicRepository
) {
    suspend operator fun invoke(
        studentId: Int,
        subjectId: Int
    ): Flow<List<Pair<Topic, Performance>>> {
        val subjectTopicIds = topicRepository.getIdsBySubject(
            subjectId = subjectId,
            withCancelled = false
        )
        return studentPerformanceRepository.getFinalPerformance(subjectTopicIds, studentId)
            .map { topicsFinalPerformance ->
                // If some topics are without performance
                if (subjectTopicIds.count() > topicsFinalPerformance.count()) {
                    val resultList = ArrayList<Pair<Topic, Performance>>(topicsFinalPerformance)
                    subjectTopicIds.forEachIndexed { index, topicId ->
                        if (!topicsFinalPerformance.any { (topic, _) -> topic.id == topicId }) {
                            val missedTopic = topicRepository.getById(topicId)!!
                            resultList.add(
                                index,
                                missedTopic to missedTopic.type.createEmptyPerformance()
                            )
                        }
                    }
                    return@map resultList
                }
                return@map topicsFinalPerformance
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
}