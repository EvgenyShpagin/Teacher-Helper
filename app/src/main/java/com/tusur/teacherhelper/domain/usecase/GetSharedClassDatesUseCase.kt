package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Date

class GetSharedClassDatesUseCase(private val getSharedClassDatetime: GetSharedClassDatetimeUseCase) {
    suspend operator fun invoke(topicId: Int, groupsIds: List<Int>): List<Date> {
        return getSharedClassDatetime(topicId, groupsIds).map { it.getDate() }.distinct()
    }
}