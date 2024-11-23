package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository
import javax.inject.Inject

class CreateTopicTypeUseCase @Inject constructor(
    private val topicTypeRepository: TopicTypeRepository
) {
    suspend operator fun invoke(topicType: TopicType) {
        topicTypeRepository.add(topicType)
    }
}