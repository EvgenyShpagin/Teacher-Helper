package com.tusur.teacherhelper.domain.model

import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.annotation.IntRange
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.number

data class Datetime(
    @IntRange(from = 2024)
    val year: Int,
    @IntRange(from = 0, to = 11)
    val month: Int,
    @IntRange(from = 0, to = 30)
    val dayOfMonth: Int,
    @IntRange(0, 23)
    val hour: Int,
    @IntRange(0, 59)
    val minute: Int
) {

    constructor(date: LocalDate) : this(
        year = date.year,
        month = date.month.number - 1,
        dayOfMonth = date.day,
        hour = 0,
        minute = 0
    )

    constructor(date: LocalDate, time: LocalTime) : this(
        year = date.year,
        month = date.month.number - 1,
        dayOfMonth = date.day,
        hour = time.hour,
        minute = time.minute
    )

    fun getDate(): LocalDate {
        return LocalDate(
            year = year,
            month = Month.entries[month],
            day = dayOfMonth
        )
    }

    fun getTime(): LocalTime {
        return LocalTime(hour = hour, minute = minute)
    }

    fun toMillis(timezoneId: String = "UTC"): Long {
        return Calendar.getInstance(TimeZone.getTimeZone(timezoneId)).also {
            it.set(year, month, dayOfMonth, hour, minute, 0)
            it.set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    companion object {
        fun fromMillis(datetimeMillis: Long, timezoneId: String = "UTC"): Datetime {
            Calendar.getInstance(TimeZone.getTimeZone(timezoneId)).also {
                it.timeInMillis = datetimeMillis
                return Datetime(
                    year = it.get(Calendar.YEAR),
                    month = it.get(Calendar.MONTH),
                    dayOfMonth = it.get(Calendar.DAY_OF_MONTH),
                    hour = it.get(Calendar.HOUR_OF_DAY),
                    minute = it.get(Calendar.MINUTE)
                )
            }
        }

        fun current(timezoneId: String = "UTC"): Datetime {
            return Calendar.getInstance(TimeZone.getTimeZone(timezoneId)).let {
                Datetime(
                    year = it.get(Calendar.YEAR),
                    month = it.get(Calendar.MONTH),
                    dayOfMonth = it.get(Calendar.DAY_OF_MONTH),
                    hour = it.get(Calendar.HOUR_OF_DAY),
                    minute = it.get(Calendar.MINUTE)
                )
            }
        }
    }
}