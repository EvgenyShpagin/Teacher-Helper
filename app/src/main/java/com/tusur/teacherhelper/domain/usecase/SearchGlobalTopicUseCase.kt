package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.util.GLOBAL_TOPICS_SUBJECT_ID

class SearchGlobalTopicUseCase(private val topicRepository: TopicRepository) {
    suspend operator fun invoke(query: String): List<Topic> {
        return topicRepository.search(GLOBAL_TOPICS_SUBJECT_ID, query)
    }
}