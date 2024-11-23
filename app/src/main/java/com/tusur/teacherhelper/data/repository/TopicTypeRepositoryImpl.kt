package com.tusur.teacherhelper.data.repository

import com.tusur.teacherhelper.data.room.dao.TopicTypeDao
import com.tusur.teacherhelper.domain.model.PRIMARY_TOPIC_TYPES_IDS
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository
import com.tusur.teacherhelper.toData
import com.tusur.teacherhelper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class TopicTypeRepositoryImpl @Inject constructor(
    private val topicTypeDao: TopicTypeDao
) : TopicTypeRepository {
    override suspend fun get(typeId: Int): TopicType? {
        return topicTypeDao.get(typeId)?.toDomain()
    }

    override fun getAll(): Flow<List<TopicType>> {
        return topicTypeDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getPrimary(): List<TopicType> {
        return topicTypeDao.getPrimary(PRIMARY_TOPIC_TYPES_IDS).map { it.toDomain() }
    }

    override suspend fun getSecondary(): List<TopicType> {
        return topicTypeDao.getSecondary(PRIMARY_TOPIC_TYPES_IDS).map { it.toDomain() }
    }

    override suspend fun update(topicType: TopicType) {
        topicTypeDao.update(topicType.toData())
    }

    override suspend fun add(topicType: TopicType) {
        topicTypeDao.insert(topicType.toData())
    }

    override suspend fun delete(topicTypeId: Int) {
        topicTypeDao.delete(topicTypeId)
    }

    override suspend fun getWithAttendance(): List<TopicType> {
        return topicTypeDao.getWithAttendance().map { it.toDomain() }
    }

    override suspend fun isTypeAppliedToAnyTopic(topicTypeId: Int): Boolean {
        return topicTypeDao.isAppliedToAnyTopic(topicTypeId)
    }
}