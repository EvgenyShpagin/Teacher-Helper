package com.tusur.teacherhelper.data.repository

import com.tusur.teacherhelper.data.room.dao.SubjectDao
import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.repository.SubjectRepository
import com.tusur.teacherhelper.toData
import com.tusur.teacherhelper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SubjectRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao
) : SubjectRepository {
    override fun getAll(): Flow<List<Subject>> {
        return subjectDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun create(subject: Subject): Int {
        return subjectDao.insert(subject.toData()).toInt()
    }

    override suspend fun getById(subjectId: Int): Subject {
        return subjectDao.getById(subjectId).toDomain()
    }

    override suspend fun getOfTopic(topicId: Int): Subject {
        return subjectDao.getOfTopic(topicId).toDomain()
    }

    override suspend fun getByName(name: String): Subject? {
        return subjectDao.getByName(name)?.toDomain()
    }
}