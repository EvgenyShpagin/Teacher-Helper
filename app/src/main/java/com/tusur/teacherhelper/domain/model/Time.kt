package com.tusur.teacherhelper.domain.model

import androidx.annotation.IntRange
import java.util.concurrent.TimeUnit

data class Time(
    @IntRange(0, 23) val hour: Int,
    @IntRange(0, 59) val minute: Int
) {
    val totalMinutes get() = minute + hour * 60

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Time

        if (hour != other.hour) return false
        if (minute != other.minute) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hour
        result = 31 * result + minute
        return result
    }

    fun toMillis(): Long {
        val hoursMs = TimeUnit.HOURS.toMillis(hour.toLong())
        val minutesMs = TimeUnit.MINUTES.toMillis(minute.toLong())
        return hoursMs + minutesMs
    }
}
