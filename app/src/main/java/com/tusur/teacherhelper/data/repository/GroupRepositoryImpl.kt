package com.tusur.teacherhelper.data.repository

import com.tusur.teacherhelper.data.room.dao.GroupDao
import com.tusur.teacherhelper.data.room.dao.StudentDao
import com.tusur.teacherhelper.data.room.dao.StudentTopicPerformanceDao
import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.repository.GroupRepository
import com.tusur.teacherhelper.toData
import com.tusur.teacherhelper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GroupRepositoryImpl(
    private val groupDao: GroupDao,
    private val studentDao: StudentDao,
    private val studentPerformance: StudentTopicPerformanceDao
) : GroupRepository {
    override suspend fun getById(id: Int): Group {
        return groupDao.getById(id).toDomain()
    }

    override fun getAll(): Flow<List<Group>> {
        return groupDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun exists(number: String): Boolean {
        return groupDao.exists(number)
    }

    override suspend fun add(group: Group): Int {
        return groupDao.insert(group.toData()).toInt()
    }

    override suspend fun deleteWithStudents(groupId: Int) {
        val studentIds = studentDao.getAllIds(groupId)
        studentIds.forEach { studentId ->
            studentPerformance.deleteAllOfStudent(studentId)
            studentDao.delete(studentId)
        }
        groupDao.delete(groupId)
    }

    override suspend fun isAssociatedToAnySubject(groupId: Int): Boolean {
        return groupDao.isAssociatedToAnySubject(groupId)
    }

    override suspend fun search(query: String): List<Group> {
        return groupDao.search(query).map { it.toDomain() }
    }

    override suspend fun hasStudents(groupId: Int): Boolean {
        return groupDao.hasStudents(groupId)
    }
}