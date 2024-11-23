package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Datetime
import javax.inject.Inject

class GetLastClassDatetimeUseCase @Inject constructor(
    private val getClassDatetimeUseCase: GetClassDatetimeUseCase
) {
    suspend operator fun invoke(topicId: Int): Datetime? {
        return getClassDatetimeUseCase(topicId).lastOrNull()
    }
}