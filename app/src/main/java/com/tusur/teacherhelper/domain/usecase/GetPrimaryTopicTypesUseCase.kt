package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository

class GetPrimaryTopicTypesUseCase(private val topicTypeRepository: TopicTypeRepository) {
    suspend operator fun invoke(): List<TopicType> {
        return topicTypeRepository.getPrimary()
    }
}
