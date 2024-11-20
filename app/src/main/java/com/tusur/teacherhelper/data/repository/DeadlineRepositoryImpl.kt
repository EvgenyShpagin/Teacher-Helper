package com.tusur.teacherhelper.data.repository

import com.tusur.teacherhelper.data.room.dao.DeadlineDao
import com.tusur.teacherhelper.domain.model.Deadline
import com.tusur.teacherhelper.domain.repository.DeadlineRepository
import com.tusur.teacherhelper.toData
import com.tusur.teacherhelper.toDomain

class DeadlineRepositoryImpl(private val deadlineDao: DeadlineDao) : DeadlineRepository {
    override suspend fun getAll(): List<Deadline> {
        return deadlineDao.getAll().map { it.toDomain() }
    }

    override suspend fun getOfTopic(topicId: Int): Deadline? {
        return deadlineDao.getOfTopic(topicId)?.toDomain()
    }

    override suspend fun delete(deadline: Deadline) {
        deadlineDao.delete(deadline.toData())
    }

    override suspend fun getOfOwningTopic(topicId: Int): Deadline? {
        return deadlineDao.getOfOwningTopic(topicId)?.toDomain()
    }
}