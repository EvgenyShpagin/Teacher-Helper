package com.tusur.teacherhelper.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tusur.teacherhelper.data.model.TopicType
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicTypeDao {

    @Query("SELECT * FROM topic_type")
    fun getAll(): Flow<List<TopicType>>

    @Query("SELECT * FROM topic_type WHERE topic_type.id = :topicTypeId")
    suspend fun get(topicTypeId: Int): TopicType?

    @Query(
        """
        SELECT 
            topic_type.*
        FROM 
            topic 
            JOIN topic_type 
            ON topic.topic_type_id = topic_type.id
        WHERE 
            topic.id = :topicId
            """
    )
    suspend fun getOfTopic(topicId: Int): TopicType

    @Insert
    suspend fun insert(topicType: TopicType)

    @Update
    suspend fun update(topicType: TopicType)

    @Query("DELETE FROM topic_type WHERE id = :topicTypeId")
    suspend fun delete(topicTypeId: Int)

    suspend fun delete(topicType: TopicType) = delete(topicType.id)

    @Query("SELECT * FROM topic_type WHERE topic_type.id IN (:primaryIds)")
    suspend fun getPrimary(primaryIds: IntArray): List<TopicType>

    @Query("SELECT * FROM topic_type WHERE topic_type.id NOT IN (:primaryIds)")
    suspend fun getSecondary(primaryIds: IntArray): List<TopicType>

    @Query("SELECT * FROM topic_type WHERE attendance_acceptable = 1")
    suspend fun getWithAttendance(): List<TopicType>

    @Query(
        """
        SELECT CASE WHEN EXISTS (
            SELECT * FROM topic WHERE topic_type_id = :topicTypeId
        ) THEN 1 
        ELSE 0 END
        """
    )
    suspend fun isAppliedToAnyTopic(topicTypeId: Int): Boolean
}