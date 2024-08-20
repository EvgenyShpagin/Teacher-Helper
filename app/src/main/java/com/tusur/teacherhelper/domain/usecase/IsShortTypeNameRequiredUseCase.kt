package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.util.TOPIC_TYPE_SHORT_NAME_MAX_LENGTH

class IsShortTypeNameRequiredUseCase {
    operator fun invoke(topicName: String?): Boolean {
        return topicName != null && topicName.length > TOPIC_TYPE_SHORT_NAME_MAX_LENGTH
    }
}