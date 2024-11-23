package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Deadline
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.DeadlineRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import javax.inject.Inject

class GetAllTopicsDeadlineUseCase @Inject constructor(
    private val deadlineRepository: DeadlineRepository,
    private val topicRepository: TopicRepository
) {
    suspend operator fun invoke(): List<Pair<Topic, Deadline>> {
        val deadlines = deadlineRepository.getAll()
        val deadlineOwnerIds = deadlines.map { it.owningTopicId }
        val deadlineOwnerTopics = topicRepository.getByIds(deadlineOwnerIds)
        return deadlineOwnerTopics.filterNotNull().mapIndexed { i, item -> item to deadlines[i] }
    }
}