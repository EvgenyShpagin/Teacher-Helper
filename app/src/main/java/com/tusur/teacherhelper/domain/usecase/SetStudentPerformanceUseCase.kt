package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository

class SetStudentPerformanceUseCase(
    private val studentPerformanceRepository: StudentPerformanceRepository,
    private val getOrAddClassDateId: GetOrAddClassDateIdUseCase
) {

    suspend operator fun invoke(
        studentId: Int,
        topicId: Int,
        grade: PerformanceItem.Grade?,
        datetimeMillis: Long
    ) {
        setPerformanceInternal(
            studentId = studentId,
            topicId = topicId,
            datetimeMillis = datetimeMillis,
            updatePerformance = { old -> old.copy(grade = grade) }
        )
    }

    suspend operator fun invoke(
        studentId: Int,
        topicId: Int,
        progress: PerformanceItem.Progress?,
        datetimeMillis: Long
    ) {
        setPerformanceInternal(
            studentId = studentId,
            topicId = topicId,
            datetimeMillis = datetimeMillis,
            updatePerformance = { old -> old.copy(progress = progress) }
        )
    }

    suspend operator fun invoke(
        studentId: Int,
        topicId: Int,
        attendance: PerformanceItem.Attendance?,
        datetimeMillis: Long
    ) {
        val wrappedAttendance = attendance?.let { listOf(it) }
        setPerformanceInternal(
            studentId = studentId,
            topicId = topicId,
            datetimeMillis = datetimeMillis,
            updatePerformance = { old -> old.copy(attendance = wrappedAttendance) }
        )
    }

    suspend operator fun invoke(
        studentId: Int,
        topicId: Int,
        assessment: PerformanceItem.Assessment?,
        datetimeMillis: Long
    ) {
        setPerformanceInternal(
            studentId = studentId,
            topicId = topicId,
            datetimeMillis = datetimeMillis,
            updatePerformance = { old -> old.copy(assessment = assessment) }
        )
    }

    private suspend fun setPerformanceInternal(
        studentId: Int,
        topicId: Int,
        datetimeMillis: Long,
        updatePerformance: (previousPerformance: Performance) -> Performance
    ) {

        val classDateId = getOrAddClassDateId(datetimeMillis)
        val currentPerformance = studentPerformanceRepository
            .getSetPerformance(topicId, studentId, classDateId)

        if (currentPerformance != null) {
            studentPerformanceRepository.update(
                studentId,
                topicId,
                classDateId,
                updatePerformance(currentPerformance)
            )
        } else {
            studentPerformanceRepository.add(
                studentId,
                topicId,
                classDateId,
                updatePerformance(Performance(null, null, null, null))
            )
        }
    }
}