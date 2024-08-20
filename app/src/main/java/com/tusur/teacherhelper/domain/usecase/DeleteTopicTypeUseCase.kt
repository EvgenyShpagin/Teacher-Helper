package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Result
import com.tusur.teacherhelper.domain.model.error.TopicTypeDeleteError
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository

class DeleteTopicTypeUseCase(
    private val topicTypeRepository: TopicTypeRepository,
    private val isTopicTypeBase: IsTopicTypeBaseUseCase,
    private val isTopicTypeUsedByAnyTopic: IsTopicTypeUsedByAnyTopicUseCase
) {
    suspend operator fun invoke(typeId: Int): Result<Unit, TopicTypeDeleteError> {
        return if (isTopicTypeBase(typeId)) {
            Result.Error(TopicTypeDeleteError.CANNOT_DELETE_BASE_TYPES)
        } else if (isTopicTypeUsedByAnyTopic(typeId)) {
            Result.Error(TopicTypeDeleteError.USED_BY_SOME_TOPICS)
        } else {
            topicTypeRepository.delete(typeId)
            Result.Success(Unit)
        }
    }
}