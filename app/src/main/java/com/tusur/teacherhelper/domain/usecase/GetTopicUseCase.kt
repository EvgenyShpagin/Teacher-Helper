package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.util.NO_ID

class GetTopicUseCase(private val topicRepository: TopicRepository) {
    suspend operator fun invoke(topicId: Int): Topic? {
        if (topicId == NO_ID) return null
        return topicRepository.getById(topicId)
    }
}