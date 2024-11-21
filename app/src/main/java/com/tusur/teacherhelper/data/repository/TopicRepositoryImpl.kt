package com.tusur.teacherhelper.data.repository

import com.tusur.teacherhelper.data.room.dao.DeadlineDao
import com.tusur.teacherhelper.data.room.dao.TopicDao
import com.tusur.teacherhelper.data.room.dao.TopicTypeDao
import com.tusur.teacherhelper.domain.model.Deadline
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.toData
import com.tusur.teacherhelper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import com.tusur.teacherhelper.data.model.Topic as DataTopic

class TopicRepositoryImpl(
    private val topicDao: TopicDao,
    private val deadlineDao: DeadlineDao,
    private val topicTypeDao: TopicTypeDao
) : TopicRepository {

    override suspend fun getById(topicId: Int): Topic? {
        val topic = topicDao.get(topicId)
        return topic?.toDomain()
    }

    override suspend fun getByIds(topicIds: List<Int>): List<Topic?> {
        return topicIds.map { getById(it) }
    }

    override fun getFlow(topicId: Int): Flow<Topic> {
        return topicDao.getFlow(topicId).filter { it != null }.map { it!!.toDomain() }
    }

    override fun getBySubject(subjectId: Int): Flow<List<Topic>> {
        return topicDao.getOfSubject(subjectId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getIdsBySubject(subjectId: Int, withCancelled: Boolean): List<Int> {
        return if (withCancelled) {
            topicDao.getIdsOfSubject(subjectId)
        } else {
            topicDao.getAllIdsOfNotCancelled(subjectId)
        }
    }

    override suspend fun getAllSameType(subjectId: Int, topicTypeId: Int): List<Topic> {
        return topicDao.getWithType(subjectId, topicTypeId).map { it.toDomain() }
    }

    override suspend fun countSameDeadlineTopics(deadlineId: Int): Int {
        return topicDao.countSameDeadlineTopics(deadlineId)
    }

    override suspend fun create(subjectId: Int, topic: Topic): Int {
        return topicDao.insert(topic.toData(subjectId, null)).toInt()
    }

    override suspend fun update(topic: Topic, deadlineId: Int?, subjectId: Int) {
        topicDao.update(topic.toData(subjectId, deadlineId))
    }

    override suspend fun delete(topicId: Int) {
        topicDao.delete(topicId)
    }

    override suspend fun getByName(topicName: Topic.Name): Topic? {
        return topicDao.getByName(topicName)?.toDomain()
    }

    override suspend fun search(subjectId: Int, query: String): List<Topic> {
        return topicDao.search(subjectId, query).map { it.toDomain() }
    }

    override suspend fun setDeadline(topicId: Int, deadline: Deadline?) {
        val currentTopic = topicDao.get(topicId) ?: return
        topicDao.update(currentTopic.copy(deadlineId = deadline?.id))
    }

    override suspend fun getOfTypes(topicTypes: List<TopicType>, subjectId: Int): List<Topic> {
        val topicTypeIds = IntArray(topicTypes.size) { topicTypes[it].id }
        return topicDao.getOfTypes(topicTypeIds, subjectId).map { it.toDomain() }
    }

    private suspend fun DataTopic.toDomain(): Topic {
        return toDomain(
            topicType = topicTypeDao.getOfTopic(id).toDomain(),
            deadline = deadlineDao.getOfTopic(id)?.toDomain()
        )
    }
}