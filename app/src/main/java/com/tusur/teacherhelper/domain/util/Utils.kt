package com.tusur.teacherhelper.domain.util

import com.tusur.teacherhelper.domain.model.Datetime
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.SumProgress
import com.tusur.teacherhelper.domain.model.TableContent
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.presentation.core.util.formatted
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.text.DecimalFormat
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


fun Topic.Name.formattedShort(): String {
    val stringBuilder = StringBuilder(24)
    stringBuilder.append(shortTypeName)
    stringBuilder.append(' ')
    if (ordinal != null) {
        stringBuilder.append(ordinal)
    } else if (date != null) {
        stringBuilder.append(date.formatted(withYear = false))
    } else if (addText != null) {
        if (addText.length > 7) {
            stringBuilder.append(addText.substring(0, 5)).append("..")
        } else {
            stringBuilder.append(addText)
        }
    }
    return stringBuilder.toString()
}

fun Topic.Name.formatted(): String {
    val stringBuilder = StringBuilder(40)
    stringBuilder.append(typeName)

    ordinal?.let { stringBuilder.append(' ').append(it) }
    addText?.let { stringBuilder.append(' ').append(it) }
    date?.let { stringBuilder.append(' ').append(it.formatted()) }
    return stringBuilder.toString()
}

fun Datetime.formatted(withYear: Boolean = true): String {
    return "${getDate().formatted(withYear)} ${getTime().formatted()}"
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

@OptIn(ExperimentalTime::class)
fun LocalDate.Companion.fromEpochMillis(
    epochMillis: Long,
    zone: TimeZone = TimeZone.currentSystemDefault()
): LocalDate {
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    return instant.toLocalDateTime(zone).date
}

@OptIn(ExperimentalTime::class)
fun LocalDate.toEpochMillis(
    zone: TimeZone = TimeZone.currentSystemDefault()
): Long {
    val instant = atStartOfDayIn(zone)
    return instant.toEpochMilliseconds()
}

/**
 * Возвращает «сегодняшнюю» дату без учёта часов, минут, секунд и миллисекунд.
 *
 * @param zone часовая зона, в которой следует определить текущую дату
 * @return LocalDate, соответствующий началу (00:00) текущего дня в указанной зоне
 */
@OptIn(ExperimentalTime::class)
fun LocalDate.Companion.today(
    zone: TimeZone = TimeZone.currentSystemDefault()
): LocalDate = Clock.System.now().toLocalDateTime(zone).date
