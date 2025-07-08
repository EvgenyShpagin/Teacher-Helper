package com.tusur.teacherhelper.domain.usecase

import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class GetLastClassDatetimeUseCase @Inject constructor(
    private val getClassDatetimeUseCase: GetClassDatetimeUseCase
) {
    suspend operator fun invoke(topicId: Int): LocalDateTime? {
        return getClassDatetimeUseCase(topicId).lastOrNull()
    }
}