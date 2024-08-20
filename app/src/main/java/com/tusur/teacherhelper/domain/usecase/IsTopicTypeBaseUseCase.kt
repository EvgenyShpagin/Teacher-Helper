package com.tusur.teacherhelper.domain.usecase

class IsTopicTypeBaseUseCase {
    operator fun invoke(topicTypeId: Int): Boolean {
        return topicTypeId in BASE_TOPIC_TYPES_ID_RANGE
    }

    companion object {
        private val BASE_TOPIC_TYPES_ID_RANGE = 1..7
    }
}
