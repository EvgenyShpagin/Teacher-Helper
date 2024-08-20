package com.tusur.teacherhelper.domain.model

data class Deadline(
    val id: Int,
    val date: Date,
    val owningTopicId: Int
)