package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Datetime
import com.tusur.teacherhelper.domain.repository.ClassDateRepository

class GetTopicClassDaysUseCase(private val classDateRepository: ClassDateRepository) {
    suspend operator fun invoke(topicId: Int): List<Datetime> {
        return classDateRepository.getOfTopic(topicId)
    }
}