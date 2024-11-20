package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.DeadlineRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository

class DeleteTopicDeadlineUseCase(
    private val topicRepository: TopicRepository,
    private val deadlineRepository: DeadlineRepository
) {
    suspend operator fun invoke(topicId: Int) {
        val topicDeadline = deadlineRepository.getOfTopic(topicId)
        topicRepository.setDeadline(topicId, null)

        if (topicDeadline?.owningTopicId == topicId) {
            deadlineRepository.delete(topicDeadline)
        } else {
            val deadlineOfTopicAsOwner = deadlineRepository.getOfOwningTopic(topicId) ?: return
            deadlineRepository.delete(deadlineOfTopicAsOwner)
        }
    }
}