package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSubjectTopicsUseCase(private val topicRepository: TopicRepository) {

    operator fun invoke(subject: Subject, withCancelled: Boolean): Flow<List<Topic>> {
        return invoke(subject.id, withCancelled)
    }

    operator fun invoke(subjectId: Int, withCancelled: Boolean): Flow<List<Topic>> {
        val topicsFlow = topicRepository.getBySubject(subjectId)
        return if (withCancelled) {
            topicsFlow
        } else {
            topicsFlow.map { topics -> topics.filter { !it.isCancelled } }
        }
    }
}