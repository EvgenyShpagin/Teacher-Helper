package com.tusur.teacherhelper.data.repository

import com.tusur.teacherhelper.data.model.SubjectGroup
import com.tusur.teacherhelper.data.room.dao.GroupDao
import com.tusur.teacherhelper.data.room.dao.SubjectGroupDao
import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.repository.SubjectGroupRepository
import com.tusur.teacherhelper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SubjectGroupRepositoryImpl(
    private val subjectGroupDao: SubjectGroupDao,
    private val groupDao: GroupDao
) : SubjectGroupRepository {
    override fun getBySubject(subjectId: Int): Flow<List<Group>> {
        val groupIds = subjectGroupDao.getAll(subjectId)
        return groupIds.map { list -> list.map { groupDao.getById(it.groupId).toDomain() } }
    }

    override suspend fun getNotEmpty(subjectId: Int): List<Group> {
        return subjectGroupDao.getNotEmpty(subjectId).map {
            groupDao.getById(it.groupId).toDomain()
        }
    }

    override suspend fun add(subjectId: Int, groupId: Int) {
        subjectGroupDao.insert(SubjectGroup(subjectId, groupId))
    }

    override suspend fun remove(subjectId: Int, groupId: Int) {
        subjectGroupDao.delete(SubjectGroup(subjectId, groupId))
    }

    override suspend fun searchGroup(subject: Subject, query: String): List<Group> {
        val subjectGroupIds = subjectGroupDao.getAll(subject.id).first().map { it.groupId }
        return groupDao.search(query).filter { it.id in subjectGroupIds }.map { it.toDomain() }
    }
}