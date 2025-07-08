package com.tusur.teacherhelper.domain.util

import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.SumProgress
import com.tusur.teacherhelper.domain.model.TableContent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


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
fun LocalDateTime.Companion.fromEpochMillis(
    epochMillis: Long,
    zone: TimeZone = TimeZone.currentSystemDefault()
): LocalDateTime {
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    return instant.toLocalDateTime(zone)
}

@OptIn(ExperimentalTime::class)
fun LocalDate.Companion.fromEpochMillis(
    epochMillis: Long,
    zone: TimeZone = TimeZone.currentSystemDefault()
): LocalDate {
    return LocalDateTime.fromEpochMillis(epochMillis, zone).date
}

@OptIn(ExperimentalTime::class)
fun LocalDateTime.toEpochMillis(
    zone: TimeZone = TimeZone.currentSystemDefault()
) = toInstant(zone).toEpochMilliseconds()


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

@OptIn(ExperimentalTime::class)
fun LocalDateTime.Companion.currentMinute(
    zone: TimeZone = TimeZone.currentSystemDefault()
): LocalDateTime {
    val now = Clock.System.now().toLocalDateTime(zone)
    return LocalDateTime(
        now.year,
        now.month,
        now.day,
        now.hour,
        now.minute,
        0,          // seconds
        0           // nanoseconds
    )
}
