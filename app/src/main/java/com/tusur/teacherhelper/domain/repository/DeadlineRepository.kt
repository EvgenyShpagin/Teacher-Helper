package com.tusur.teacherhelper.domain.repository

import com.tusur.teacherhelper.domain.model.Deadline

interface DeadlineRepository {
    suspend fun getAll(): List<Deadline>
    suspend fun getOfTopic(topicId: Int): Deadline?
    suspend fun delete(deadline: Deadline)
}