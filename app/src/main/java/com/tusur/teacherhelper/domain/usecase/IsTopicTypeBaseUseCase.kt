package com.tusur.teacherhelper.domain.usecase

import javax.inject.Inject

class IsTopicTypeBaseUseCase @Inject constructor() {
    operator fun invoke(topicTypeId: Int): Boolean {
        return topicTypeId in BASE_TOPIC_TYPES_ID_RANGE
    }

    companion object {
        private val BASE_TOPIC_TYPES_ID_RANGE = 1..7
    }
}
