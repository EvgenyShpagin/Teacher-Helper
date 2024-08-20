package com.tusur.teacherhelper.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tusur.teacherhelper.data.model.Deadline

@Dao
interface DeadlineDao {
    @Query("SELECT * FROM deadline")
    suspend fun getAll(): List<Deadline>

    @Query(
        """SELECT 
                deadline.*
            FROM 
                deadline 
                JOIN 
                    topic 
                ON topic.deadline_id = deadline.id 
            WHERE topic.id = :topicId
        """
    )
    suspend fun getOfTopic(topicId: Int): Deadline?

    @Query("SELECT * FROM deadline WHERE deadline.creator_topic_id = :topicId")
    suspend fun getOfOwningTopic(topicId: Int): Deadline?

    @Insert
    suspend fun insert(deadline: Deadline): Long

    @Query("DELETE FROM deadline WHERE id = :deadlineId")
    suspend fun delete(deadlineId: Int)

    suspend fun delete(deadline: Deadline) = delete(deadline.id)

    @Query("DELETE FROM deadline WHERE creator_topic_id = :topicId")
    suspend fun deleteByCreator(topicId: Int)
}