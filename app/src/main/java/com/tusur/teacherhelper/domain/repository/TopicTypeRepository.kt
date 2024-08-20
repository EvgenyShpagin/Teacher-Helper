package com.tusur.teacherhelper.domain.repository

import com.tusur.teacherhelper.domain.model.TopicType
import kotlinx.coroutines.flow.Flow


interface TopicTypeRepository {
    suspend fun get(typeId: Int): TopicType?
    suspend fun getPrimary(): List<TopicType>
    suspend fun getSecondary(): List<TopicType>
    suspend fun update(topicType: TopicType)
    suspend fun add(topicType: TopicType)
    suspend fun delete(topicTypeId: Int)
    fun getAll(): Flow<List<TopicType>>
    suspend fun getWithAttendance(): List<TopicType>
    suspend fun isTypeAppliedToAnyTopic(topicTypeId: Int): Boolean
}