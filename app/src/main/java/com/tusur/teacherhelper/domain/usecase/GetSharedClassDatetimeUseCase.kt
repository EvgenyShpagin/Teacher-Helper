package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Datetime
import com.tusur.teacherhelper.domain.repository.ClassDateRepository

class GetSharedClassDatetimeUseCase(private val classDateRepository: ClassDateRepository) {
    suspend operator fun invoke(topicId: Int, groupsIds: List<Int>): List<Datetime> {
        return classDateRepository.getShared(topicId, groupsIds)
    }
}