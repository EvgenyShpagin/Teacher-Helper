package com.tusur.teacherhelper.domain.model

data class ClassTime(
    val initTime: Time,
    val finishTime: Time
) {
    operator fun contains(time: Time): Boolean {
        return time.totalMinutes in initTime.totalMinutes..finishTime.totalMinutes
    }
}