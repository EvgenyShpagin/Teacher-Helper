package com.tusur.teacherhelper.domain.usecase

import javax.inject.Inject

class DoesTopicHaveClassDatetimeUseCase @Inject constructor(
    private val getClassDatetimeUseCase: GetClassDatetimeUseCase
) {
    suspend operator fun invoke(topicId: Int): Boolean {
        return getClassDatetimeUseCase(topicId).isNotEmpty()
    }
}