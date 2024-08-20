package com.tusur.teacherhelper.domain.model.error

enum class TopicTypeDeleteError : Error {
    CANNOT_DELETE_BASE_TYPES,
    USED_BY_SOME_TOPICS
}