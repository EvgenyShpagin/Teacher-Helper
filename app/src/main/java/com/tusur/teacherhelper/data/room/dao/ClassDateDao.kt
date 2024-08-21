package com.tusur.teacherhelper.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tusur.teacherhelper.data.model.ClassDate

@Dao
interface ClassDateDao {
    @Query(
        """
        SELECT
            date.*
        FROM
            class_date date
            JOIN student_topic_performance stp 
                ON date.id = stp.class_date_id
                AND stp.topic_id = :topicId
            JOIN topic
                ON stp.topic_id = topic.id
            JOIN student
                ON student.id = stp.student_id
                AND student.group_id in (:groupsIds)
        ORDER BY 
            date.datetime_millis
        """
    )
    suspend fun getShared(topicId: Int, groupsIds: List<Int>): List<ClassDate>

    @Query(
        """
        SELECT DISTINCT
            class_date.*
        FROM
            topic
            JOIN student_topic_performance stp
                ON stp.topic_id = topic.id 
                AND topic.id = :topicId
            JOIN class_date
                ON class_date.id = stp.class_date_id
        ORDER BY 
            class_date.datetime_millis
    """
    )
    suspend fun getOfTopic(topicId: Int): List<ClassDate>

    @Query("""SELECT * FROM class_date WHERE class_date.id = :classDateId""")
    suspend fun getById(classDateId: Int): ClassDate?

    @Query("""SELECT * FROM class_date WHERE class_date.datetime_millis = :datetimeMillis""")
    suspend fun getByDatetimeMillis(datetimeMillis: Long): ClassDate?

    @Insert
    suspend fun insert(classDate: ClassDate): Long

    @Update
    suspend fun update(classDate: ClassDate)

    @Delete
    suspend fun delete(classDate: ClassDate)
}