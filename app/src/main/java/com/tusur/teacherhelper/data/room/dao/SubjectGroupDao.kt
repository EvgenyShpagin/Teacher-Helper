package com.tusur.teacherhelper.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.tusur.teacherhelper.data.model.SubjectGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectGroupDao {

    @Query("SELECT * FROM subject_group WHERE subject_group.subject_id = :subjectId")
    fun getAll(subjectId: Int): Flow<List<SubjectGroup>>

    @Delete
    suspend fun delete(subjectGroup: SubjectGroup)

    @Insert
    suspend fun insert(subjectGroup: SubjectGroup)

    @Query(
        """
        SELECT 
            DISTINCT subject_group.*
        FROM
            subject_group
            JOIN student ON student.group_id = subject_group.group_id
        WHERE 
            subject_group.subject_id = :subjectId
            """
    )
    suspend fun getNotEmpty(subjectId: Int): List<SubjectGroup>
}