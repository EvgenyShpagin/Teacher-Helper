package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository

class GetTopicTypeUseCase(private val topicTypeRepository: TopicTypeRepository) {
    suspend operator fun invoke(typeId: Int): TopicType? {
        return topicTypeRepository.get(typeId)
    }
}