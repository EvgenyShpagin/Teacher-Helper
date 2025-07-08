package com.tusur.teacherhelper.domain.repository

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface ClassDateRepository {
    suspend fun update(topicId: Int, oldDatetimeMs: Long, newDatetimeMs: Long)
    suspend fun getShared(topicId: Int, groupsIds: List<Int>): List<LocalDateTime>
    suspend fun getOfTopic(topicId: Int): List<LocalDateTime>
    suspend fun getId(date: LocalDate): Int?
    suspend fun getIdByMillis(datetimeMillis: Long): Int?
    suspend fun add(datetimeMillis: Long): Int
    suspend fun delete(datetimeMillis: Long)
}