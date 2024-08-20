package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic

class CancelTopicUseCase(private val updateSubjectTopic: UpdateSubjectTopicUseCase) {
    suspend operator fun invoke(topic: Topic) {
        updateSubjectTopic(topic.copy(isCancelled = true))
    }
}