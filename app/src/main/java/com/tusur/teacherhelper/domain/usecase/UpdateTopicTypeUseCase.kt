package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository

class UpdateTopicTypeUseCase(private val topicTypeRepository: TopicTypeRepository) {
    suspend operator fun invoke(topicType: TopicType) {
        topicTypeRepository.update(topicType)
    }
}