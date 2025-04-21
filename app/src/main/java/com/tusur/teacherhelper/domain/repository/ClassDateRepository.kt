package com.tusur.teacherhelper.domain.repository

import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.domain.model.Datetime

interface ClassDateRepository {
    suspend fun update(topicId: Int, oldDatetimeMs: Long, newDatetimeMs: Long)
    suspend fun getShared(topicId: Int, groupsIds: List<Int>): List<Datetime>
    suspend fun getOfTopic(topicId: Int): List<Datetime>
    suspend fun getId(date: Date): Int?
    suspend fun getIdByMillis(datetimeMillis: Long): Int?
    suspend fun add(datetimeMillis: Long): Int
    suspend fun delete(datetimeMillis: Long)
}