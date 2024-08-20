package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class GetTopicAsFlowUseCase(private val topicRepository: TopicRepository) {
    operator fun invoke(topicId: Int): Flow<Topic> {
        return topicRepository.getFlow(topicId).distinctUntilChanged()
    }
}