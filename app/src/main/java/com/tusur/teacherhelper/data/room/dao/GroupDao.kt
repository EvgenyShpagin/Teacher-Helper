package com.tusur.teacherhelper.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tusur.teacherhelper.data.model.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM `group`")
    fun getAll(): Flow<List<Group>>

    @Query("SELECT * FROM `group` WHERE id = :id")
    suspend fun getById(id: Int): Group

    @Query(
        """
        SELECT CASE WHEN EXISTS (
            SELECT 
                * 
            FROM
                subject_group
            WHERE
                group_id = :groupId
        ) THEN 1
        ELSE 0 END
    """
    )
    suspend fun isAssociatedToAnySubject(groupId: Int): Boolean

    @Query("SELECT * FROM `group` WHERE number LIKE :query")
    suspend fun search(query: String): List<Group>

    @Query(
        """
        SELECT CASE WHEN EXISTS (
            SELECT
                *
            FROM 
                `group`
            WHERE 
                `group`.number = :number
        )
        THEN 1
        ELSE 0 END
    """
    )
    suspend fun exists(number: String): Boolean

    @Insert
    suspend fun insert(group: Group): Long

    @Query("DELETE FROM `group` WHERE id = :groupId")
    suspend fun delete(groupId: Int)

    @Query(
        """
        SELECT CASE WHEN EXISTS (
            SELECT
                *
            FROM 
                student
            WHERE 
                group_id = :groupId
        )
        THEN 1
        ELSE 0 END
    """
    )
    suspend fun hasStudents(groupId: Int): Boolean
}