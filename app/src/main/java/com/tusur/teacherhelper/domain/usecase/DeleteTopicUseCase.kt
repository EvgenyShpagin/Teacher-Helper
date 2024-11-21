package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.SubjectGroupRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import kotlinx.coroutines.flow.first

class DeleteTopicUseCase(
    private val topicRepository: TopicRepository,
    private val subjectGroupRepository: SubjectGroupRepository,
    private val setTopicDeadline: SetTopicDeadlineUseCase,
    private val deletePerformance: DeletePerformanceUseCase,
    private val getClassDatetime: GetClassDatetimeUseCase
) {
    suspend operator fun invoke(topicId: Int, subjectId: Int) {
        setTopicDeadline(topicId, null)
        val classDays = getClassDatetime(topicId)
        val groups = subjectGroupRepository.getBySubject(subjectId).first()
        deletePerformance(topicId, groups.map { it.id }, classDays)
        topicRepository.delete(topicId)
    }
}
