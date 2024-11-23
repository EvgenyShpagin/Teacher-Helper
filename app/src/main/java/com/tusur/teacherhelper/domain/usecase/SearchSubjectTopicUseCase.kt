package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.TopicRepository
import javax.inject.Inject

class SearchSubjectTopicUseCase @Inject constructor(private val topicRepository: TopicRepository) {
    suspend operator fun invoke(subjectId: Int, query: String): List<Topic> {
        return topicRepository.search(subjectId, query)
    }
}
