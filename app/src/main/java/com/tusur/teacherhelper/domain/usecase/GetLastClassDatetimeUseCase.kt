package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Datetime

class GetLastClassDatetimeUseCase(private val getClassDatetimeUseCase: GetClassDatetimeUseCase) {
    suspend operator fun invoke(topicId: Int): Datetime? {
        return getClassDatetimeUseCase(topicId).lastOrNull()
    }
}