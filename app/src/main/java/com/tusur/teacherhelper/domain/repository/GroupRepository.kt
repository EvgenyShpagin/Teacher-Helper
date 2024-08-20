package com.tusur.teacherhelper.domain.repository

import com.tusur.teacherhelper.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getAll(): Flow<List<Group>>
    suspend fun getById(id: Int): Group
    suspend fun exists(number: String): Boolean
    suspend fun add(group: Group): Int
    suspend fun deleteWithStudents(groupId: Int)
    suspend fun isAssociatedToAnySubject(groupId: Int): Boolean
    suspend fun search(query: String): List<Group>
    suspend fun hasStudents(groupId: Int): Boolean
}