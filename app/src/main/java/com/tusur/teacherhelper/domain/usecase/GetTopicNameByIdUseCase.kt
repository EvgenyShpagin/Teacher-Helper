package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.TopicRepository
import javax.inject.Inject

class GetTopicNameByIdUseCase @Inject constructor(private val topicRepository: TopicRepository) {
    suspend operator fun invoke(topicId: Int): Topic.Name {
        return topicRepository.getById(topicId)!!.name
    }
}
