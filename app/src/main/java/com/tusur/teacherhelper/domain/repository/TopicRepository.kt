package com.tusur.teacherhelper.domain.repository

import com.tusur.teacherhelper.domain.model.Deadline
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.TopicType
import kotlinx.coroutines.flow.Flow

interface TopicRepository {
    suspend fun getById(topicId: Int): Topic?
    fun getFlow(topicId: Int): Flow<Topic>
    suspend fun getByIds(topicIds: List<Int>): List<Topic?>
    fun getBySubject(subjectId: Int): Flow<List<Topic>>
    suspend fun getIdsBySubject(subjectId: Int, withCancelled: Boolean): List<Int>
    suspend fun getAllSameType(subjectId: Int, topicTypeId: Int): List<Topic>
    suspend fun countSameDeadlineTopics(deadlineId: Int): Int
    suspend fun create(subjectId: Int, topic: Topic): Int
    suspend fun update(topic: Topic, deadlineId: Int?, subjectId: Int)
    suspend fun delete(topicId: Int)
    suspend fun getByName(topicName: Topic.Name): Topic?
    suspend fun search(subjectId: Int, query: String): List<Topic>
    suspend fun setDeadline(topicId: Int, deadline: Deadline?)
    suspend fun getOfTypes(topicTypes: List<TopicType>, subjectId: Int): List<Topic>
}