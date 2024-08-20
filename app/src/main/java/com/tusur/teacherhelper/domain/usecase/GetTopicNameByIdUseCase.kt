package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.TopicRepository

class GetTopicNameByIdUseCase(private val topicRepository: TopicRepository) {
    suspend operator fun invoke(topicId: Int): Topic.Name {
        return topicRepository.getById(topicId)!!.name
    }
}
