package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.TopicTypeRepository

class IsTopicTypeUsedByAnyTopicUseCase(private val topicTypeRepository: TopicTypeRepository) {
    suspend operator fun invoke(topicTypeId: Int): Boolean {
        return topicTypeRepository.isTypeAppliedToAnyTopic(topicTypeId)
    }
}
