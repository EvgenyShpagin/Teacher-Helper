package com.tusur.teacherhelper.domain.model

import kotlinx.datetime.LocalDate

data class Deadline(
    val id: Int,
    val date: LocalDate,
    val owningTopicId: Int
)