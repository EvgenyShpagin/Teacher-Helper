package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.util.GLOBAL_TOPICS_SUBJECT_ID
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGlobalTopicsUseCase @Inject constructor(
    private val topicRepository: TopicRepository
) {
    operator fun invoke(): Flow<List<Topic>> {
        return topicRepository.getBySubject(GLOBAL_TOPICS_SUBJECT_ID)
    }
}