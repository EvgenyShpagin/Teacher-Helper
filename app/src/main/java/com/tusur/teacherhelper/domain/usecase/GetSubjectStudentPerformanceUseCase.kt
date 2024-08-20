package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSubjectStudentPerformanceUseCase(
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
                            resultList.add(index, missedTopic to NO_PERFORMANCE)
                        }
                    }
                    return@map resultList
                }
                return@map topicsFinalPerformance
            }
    }

    private companion object {
        val NO_PERFORMANCE = Performance(null, null, null, null)
    }
}