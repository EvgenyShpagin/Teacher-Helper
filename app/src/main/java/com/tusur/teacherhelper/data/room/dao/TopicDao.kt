package com.tusur.teacherhelper.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tusur.teacherhelper.data.model.Topic
import com.tusur.teacherhelper.domain.model.Topic.Name
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    @Query("SELECT * FROM topic WHERE topic.id = :topicId")
    suspend fun get(topicId: Int): Topic?

    @Query("SELECT * FROM topic WHERE topic.id = :topicId")
    fun getFlow(topicId: Int): Flow<Topic?>

    @Query("SELECT * FROM topic WHERE topic.subject_id = :subjectId")
    fun getOfSubject(subjectId: Int): Flow<List<Topic>>

    @Query("SELECT id FROM topic WHERE topic.subject_id = :subjectId")
    suspend fun getIdsOfSubject(subjectId: Int): List<Int>

    @Query("SELECT id FROM topic WHERE topic.subject_id = :subjectId AND topic.cancelled = 0")
    suspend fun getAllIdsOfNotCancelled(subjectId: Int): List<Int>

    @Query(
        """
        SELECT 
            topic.* 
        FROM 
            topic
            JOIN topic_type ON topic_type.id = topic.topic_type_id
        WHERE 
            topic.subject_id = :subjectId
            AND topic_type.id in (:topicTypeIds)
            """
    )
    suspend fun getOfTypes(topicTypeIds: IntArray, subjectId: Int): List<Topic>

    @Query(
        """
        SELECT
            *
        FROM 
            topic
        WHERE
            topic.subject_id = :subjectId
            AND topic.topic_type_id = :topicTypeId
    """
    )
    suspend fun getWithType(subjectId: Int, topicTypeId: Int): List<Topic>

    @Query("SELECT * FROM topic WHERE topic.name = :topicName")
    suspend fun getByName(topicName: Name): Topic?

    @Query("SELECT * FROM topic WHERE topic.subject_id = :subjectId AND topic.name LIKE :query")
    suspend fun search(subjectId: Int, query: String): List<Topic>

    @Update
    suspend fun update(topic: Topic)

    @Insert
    suspend fun insert(topic: Topic): Long

    @Delete
    suspend fun delete(topic: Topic)

    @Query("DELETE FROM topic WHERE id = :topicId")
    suspend fun delete(topicId: Int)

    @Query(
        """
        SELECT 
            COUNT(*) 
        FROM 
            deadline
            JOIN topic ON deadline.id = topic.deadline_id
        WHERE 
            topic.deadline_id = :deadlineId
        """
    )
    suspend fun countSameDeadlineTopics(deadlineId: Int): Int
}