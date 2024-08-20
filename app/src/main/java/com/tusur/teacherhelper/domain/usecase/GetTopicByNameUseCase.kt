package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.TopicRepository

class GetTopicByNameUseCase(private val topicRepository: TopicRepository) {
    suspend operator fun invoke(name: Topic.Name): Topic? {
        return topicRepository.getByName(name)
    }
}
