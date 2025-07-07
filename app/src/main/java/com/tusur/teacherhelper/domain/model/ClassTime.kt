package com.tusur.teacherhelper.domain.model

import kotlinx.datetime.LocalTime

data class ClassTime(
    val initTime: LocalTime,
    val finishTime: LocalTime
) {
    @Deprecated("Move to specific package and test")
    operator fun contains(time: LocalTime): Boolean {
        return time in initTime..finishTime
    }
}