package com.tusur.teacherhelper.domain.usecase

class DoesTopicHaveClassDatetimeUseCase(private val getClassDatetimeUseCase: GetClassDatetimeUseCase) {
    suspend operator fun invoke(topicId: Int): Boolean {
        return getClassDatetimeUseCase(topicId).isNotEmpty()
    }
}