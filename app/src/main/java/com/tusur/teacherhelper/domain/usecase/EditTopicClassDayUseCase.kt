package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.ClassDateRepository

class EditTopicClassDayUseCase(private val classDateRepository: ClassDateRepository) {
    suspend operator fun invoke(topicId: Int, oldDatetimeMs: Long, newDatetimeMs: Long) {
        classDateRepository.update(topicId, oldDatetimeMs, newDatetimeMs)
    }
}