package com.tusur.teacherhelper.domain.repository

import com.tusur.teacherhelper.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    fun getAll(): Flow<List<Subject>>
    suspend fun create(subject: Subject): Int
    suspend fun getById(subjectId: Int): Subject
    suspend fun getOfTopic(topicId: Int): Subject
    suspend fun getByName(name: String): Subject?
}