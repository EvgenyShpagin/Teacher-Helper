package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicRepository
import javax.inject.Inject

class GetTopicTypeByTopicUseCase @Inject constructor(private val topicRepository: TopicRepository) {
    suspend operator fun invoke(topicId: Int): TopicType {
        return topicRepository.getById(topicId)!!.type
    }
}