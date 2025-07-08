package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.ClassDateRepository
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class GetSharedClassDatetimeUseCase @Inject constructor(
    private val classDateRepository: ClassDateRepository
) {
    suspend operator fun invoke(topicId: Int, groupsIds: List<Int>): List<LocalDateTime> {
        return classDateRepository.getShared(topicId, groupsIds)
    }
}