package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetTopicTypesUseCase @Inject constructor(
    private val topicTypeRepository: TopicTypeRepository
) {
    operator fun invoke(): Flow<List<TopicType>> {
        return topicTypeRepository.getAll()
    }
}