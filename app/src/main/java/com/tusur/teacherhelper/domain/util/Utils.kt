package com.tusur.teacherhelper.domain.util

import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.domain.model.Datetime
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.SumProgress
import com.tusur.teacherhelper.domain.model.TableContent
import com.tusur.teacherhelper.domain.model.Topic
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import java.text.DateFormat
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


fun Topic.Name.formattedShort(locale: Locale): String {
    val stringBuilder = StringBuilder(24)
    stringBuilder.append(shortTypeName)
    stringBuilder.append(' ')
    if (ordinal != null) {
        stringBuilder.append(ordinal)
    } else if (date != null) {
        stringBuilder.append(date.formatted(locale, withYear = false))
    } else if (addText != null) {
        if (addText.length > 7) {
            stringBuilder.append(addText.substring(0, 5)).append("..")
        } else {
            stringBuilder.append(addText)
        }
    }
    return stringBuilder.toString()
}

fun Topic.Name.formatted(locale: Locale): String {
    val stringBuilder = StringBuilder(40)
    stringBuilder.append(typeName)

    ordinal?.let { stringBuilder.append(' ').append(it) }
    addText?.let { stringBuilder.append(' ').append(it) }
    date?.let { stringBuilder.append(' ').append(it.formatted(locale)) }
    return stringBuilder.toString()
}

fun Datetime.formatted(
    locale: Locale,
    withYear: Boolean = true,
    timezoneId: String = "UTC"
): String {
    return "${getDate().formatted(locale, withYear, timezoneId)} ${getTime().formatted()}"
}

fun LocalTime.formatted(): String {
    return format(
        LocalTime.Format {
            hour()
            char(':')
            minute()
        }
    )
}

fun Date.formatted(locale: Locale, withYear: Boolean = true, timezoneId: String = "UTC"): String {
    val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale)

    val calendar = Calendar.getInstance()

    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

    dateFormat.timeZone = TimeZone.getTimeZone(timezoneId)

    val formattedDate = dateFormat.format(calendar.time)

    if (withYear) {
        return formattedDate
    }
    val yearAsString = year.toString()
    val yearIsFull = formattedDate.contains(yearAsString)
    val yearLength = if (yearIsFull) {
        4
    } else {
        2
    }
    val yearFromLeft =
        formattedDate.substring(0, yearLength) == yearAsString.substring(0, yearLength)
    return if (yearFromLeft) {
        formattedDate.drop(yearLength + 1)
    } else {
        formattedDate.dropLast(yearLength + 1)
    }
}

val Student.shortName: String
    get() {
        return if (name.middleName.isNotEmpty()) {
            "${name.lastName} ${name.firstName.first()}. ${name.middleName.first()}."
        } else {
            "${name.lastName} ${name.firstName.first()}."
        }
    }

fun PerformanceItem.Progress.inPercentage(): Int {
    return (value * 100).toInt()
}

fun <T, R> TableContent<T>.map(
    transformLabels: Boolean = true,
    transform: (T) -> R
) = TableContent(columnCount, rowCount) { index ->
    get(index).map { transform(it) }
}.also { resultTable ->
    if (transformLabels) {
        resultTable.columnLabels = columnLabels.map { transform(it) }
    }
}

private val doubleShortDecimalFormat = DecimalFormat("0.#")

fun Double.formatted(): String {
    return doubleShortDecimalFormat.format(this)
}

fun Float.formatted(): String {
    return toDouble().formatted()
}

fun List<PerformanceItem.Attendance>.getTotalAttendance(): SumProgress<Int> {
    return SumProgress(count { it !is PerformanceItem.Attendance.Absent }, count())
}

fun String.withoutUnwantedSpaces(): String {
    return trim().replace(SPACE_REGEX, " ")
}

const val NO_ID = 0
const val GLOBAL_TOPICS_SUBJECT_ID = -37
const val TOPIC_TYPE_SHORT_NAME_MAX_LENGTH = 6

val PROHIBITED_GROUP_CHARS_REGEX = "[^0-9а-яА-Я\\-]".toRegex()

val SPACE_REGEX = "\\s+".toRegex()

fun <S, T> List<S>.applyOrderOf(
    targetOrderList: List<T>,
    comparator: (S, T) -> Boolean
): List<Pair<S, T>> {
    val pairedTargetOrderList = targetOrderList.filter { targetOrderItem ->
        any { comparator(it, targetOrderItem) }
    }
    val orderedList = ArrayList<S>(pairedTargetOrderList.count())
    pairedTargetOrderList.forEach { targetItem ->
        find { comparator(it, targetItem) }?.let { orderedList.add(it) }
    }
    return List(orderedList.count()) { index ->
        orderedList[index] to pairedTargetOrderList[index]
    }
}