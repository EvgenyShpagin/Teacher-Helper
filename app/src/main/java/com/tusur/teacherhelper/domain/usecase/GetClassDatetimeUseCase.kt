package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.ClassDateRepository
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject


class GetClassDatetimeUseCase @Inject constructor(
    private val classDateRepository: ClassDateRepository
) {
    suspend operator fun invoke(topicId: Int): List<LocalDateTime> {
        return classDateRepository.getOfTopic(topicId)
    }
}