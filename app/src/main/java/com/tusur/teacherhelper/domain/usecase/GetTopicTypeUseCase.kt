package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository
import javax.inject.Inject

class GetTopicTypeUseCase @Inject constructor(
    private val topicTypeRepository: TopicTypeRepository
) {
    suspend operator fun invoke(typeId: Int): TopicType? {
        return topicTypeRepository.get(typeId)
    }
}