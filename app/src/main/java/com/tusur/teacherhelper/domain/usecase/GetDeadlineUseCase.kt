package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Deadline
import com.tusur.teacherhelper.domain.repository.DeadlineRepository
import javax.inject.Inject

class GetDeadlineUseCase @Inject constructor(private val deadlineRepository: DeadlineRepository) {
    suspend operator fun invoke(topicId: Int): Deadline? {
        return deadlineRepository.getOfTopic(topicId)
    }
}