package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository
import javax.inject.Inject

class SearchTopicTypeUseCase @Inject constructor(
    private val topicTypeRepository: TopicTypeRepository
) {
    suspend operator fun invoke(query: String): List<TopicType> {
//        return topicTypeRepository.search(query) // TODO: implement
        return emptyList()
    }
}