package com.tusur.teacherhelper.domain.repository

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectGroupRepository {
    fun getBySubject(subjectId: Int): Flow<List<Group>>
    suspend fun getNotEmpty(subjectId: Int): List<Group>
    suspend fun add(subjectId: Int, groupId: Int)
    suspend fun remove(subjectId: Int, groupId: Int)
    suspend fun searchGroup(subject: Subject, query: String): List<Group>
}