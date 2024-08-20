package com.tusur.teacherhelper.domain.model

import androidx.annotation.IntRange

data class Date(
    @IntRange(from = 2024)
    val year: Int,
    @IntRange(from = 0, to = 11)
    val month: Int,
    @IntRange(from = 0, to = 30)
    val dayOfMonth: Int
) {
    fun toMillis(timezoneId: String = "UTC"): Long {
        return Datetime(this).toMillis(timezoneId)
    }

    override fun hashCode(): Int {
        var result = year
        result = 31 * result + month
        result = 31 * result + dayOfMonth
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Date

        if (year != other.year) return false
        if (month != other.month) return false
        if (dayOfMonth != other.dayOfMonth) return false

        return true
    }

    companion object {
        fun fromMillis(millis: Long, timezoneId: String = "UTC"): Date {
            return Datetime.fromMillis(millis, timezoneId).getDate()
        }

        fun current(): Date {
            return Datetime.current().getDate()
        }
    }
}