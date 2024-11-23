package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.DeadlineRepository
import com.tusur.teacherhelper.domain.repository.SubjectRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import javax.inject.Inject

class UpdateSubjectTopicUseCase @Inject constructor(
    private val topicRepository: TopicRepository,
    private val subjectRepository: SubjectRepository,
    private val deadlineRepository: DeadlineRepository
) {
    suspend operator fun invoke(topic: Topic) {
        val subject = subjectRepository.getOfTopic(topic.id)
        val deadline = deadlineRepository.getOfTopic(topic.id)
        topicRepository.update(topic, deadline?.id, subject.id)
    }
}