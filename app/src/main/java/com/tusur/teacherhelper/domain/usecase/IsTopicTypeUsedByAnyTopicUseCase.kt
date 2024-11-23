package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.TopicTypeRepository
import javax.inject.Inject

class IsTopicTypeUsedByAnyTopicUseCase @Inject constructor(
    private val topicTypeRepository: TopicTypeRepository
) {
    suspend operator fun invoke(topicTypeId: Int): Boolean {
        return topicTypeRepository.isTypeAppliedToAnyTopic(topicTypeId)
    }
}
