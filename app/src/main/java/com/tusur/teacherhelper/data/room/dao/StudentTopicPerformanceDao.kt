package com.tusur.teacherhelper.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.tusur.teacherhelper.data.model.StudentTopicPerformance
import com.tusur.teacherhelper.domain.model.PerformanceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentTopicPerformanceDao {

    @Query(
        """
        SELECT 
            stp.*
        FROM 
            student_topic_performance stp
        WHERE 
            stp.class_date_id = :classDateId
            AND stp.student_id = :studentId
            AND stp.topic_id = :topicId
        """
    )
    suspend fun get(topicId: Int, studentId: Int, classDateId: Int): StudentTopicPerformance?

    @Query(
        """
        SELECT 
            stp.*
        FROM 
            student_topic_performance stp
        WHERE 
            stp.class_date_id = :classDateId
            AND stp.student_id IN (:studentIds)
            AND stp.topic_id = :topicId
        """
    )
    fun getAll(
        topicId: Int,
        studentIds: IntArray,
        classDateId: Int
    ): Flow<List<StudentTopicPerformance>>

    @Query(
        """
        SELECT 
            stp.*
        FROM
            student_topic_performance stp
            JOIN class_date cd ON cd.id = stp.class_date_id
        WHERE
            stp.student_id = :studentId
            AND stp.topic_id IN (:topicIds)
            AND cd.datetime_millis = (
                SELECT 
                    MAX(inner_cd.datetime_millis)
                FROM 
                    student_topic_performance inner_stp
                    JOIN class_date inner_cd ON inner_cd.id = inner_stp.class_date_id
                WHERE 
                    inner_stp.student_id = stp.student_id
                    AND inner_stp.topic_id = stp.topic_id
            )
    """
    )
    fun getFinalPerformance(
        topicIds: List<Int>,
        studentId: Int
    ): Flow<List<StudentTopicPerformance>>

    @Query(
        """
        SELECT
            stp.*
        FROM 
            student_topic_performance stp
        WHERE
            topic_id in (:topicIds)
            AND student_id = :studentId
    """
    )
    suspend fun getOfStudent(topicIds: List<Int>, studentId: Int): List<StudentTopicPerformance>

    @Query(
        "INSERT INTO student_topic_performance " +
                "VALUES (:studentId, :topicId, :classDateId, :grade, :progress, :attendance, :assessment)"
    )
    suspend fun insert(
        studentId: Int,
        topicId: Int,
        classDateId: Int,
        grade: PerformanceItem.Grade? = null,
        progress: PerformanceItem.Progress? = null,
        attendance: PerformanceItem.Attendance? = null,
        assessment: PerformanceItem.Assessment? = null
    )

    @Update
    suspend fun update(studentTopicPerformance: StudentTopicPerformance)

    @Query(
        """
        DELETE FROM student_topic_performance 
        WHERE topic_id = :topicId AND class_date_id IN (:classDateIds) AND student_id IN (:studentIds)
    """
    )
    suspend fun delete(topicId: Int, studentIds: List<Int>, classDateIds: List<Int>)

    @Query(
        """
        DELETE FROM student_topic_performance
        WHERE student_id = :studentId
    """
    )
    suspend fun deleteAllOfStudent(studentId: Int)
}