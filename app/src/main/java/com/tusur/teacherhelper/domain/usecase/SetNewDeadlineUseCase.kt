package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Deadline
import com.tusur.teacherhelper.domain.repository.TopicRepository

class SetNewDeadlineUseCase(
    private val topicRepository: TopicRepository,
    private val deleteTopicDeadline: DeleteTopicDeadlineUseCase
) {
    suspend operator fun invoke(topicId: Int, deadline: Deadline?) {
        deleteTopicDeadline(topicId)
        if (deadline == null) return
        topicRepository.setDeadline(topicId, deadline)
    }
}