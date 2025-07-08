package com.tusur.teacherhelper.domain.repository

import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface StudentPerformanceRepository {
    suspend fun getSetPerformance(
        topicId: Int,
        studentId: Int,
        classDateId: Int
    ): Performance?

    suspend fun getSetPerformance(
        topicId: Int,
        students: List<Student>,
        classDateId: Int
    ): Flow<List<Pair<Student, Performance>>>

    suspend fun getFinalPerformance(
        topicIds: List<Int>,
        studentId: Int
    ): Flow<List<Pair<Topic, Performance>>>

    suspend fun getFinalPerformanceClassDayDatetimeMs(studentId: Int, topicId: Int): Long?

    suspend fun getAttendance(
        topicIds: List<Int>,
        studentId: Int
    ): List<Pair<Topic, PerformanceItem.Attendance?>>

    suspend fun getSetPerformance(
        topicIds: List<Int>,
        studentId: Int
    ): List<Pair<Topic, Performance>>

    suspend fun deletePerformance(
        topicId: Int,
        groupListIds: List<Int>,
        datetime: List<LocalDateTime>
    )

    suspend fun deleteAllTopic(topicId: Int)

    suspend fun update(studentId: Int, topicId: Int, classDateId: Int, performance: Performance)
    suspend fun add(studentId: Int, topicId: Int, classDateId: Int, performance: Performance)
}