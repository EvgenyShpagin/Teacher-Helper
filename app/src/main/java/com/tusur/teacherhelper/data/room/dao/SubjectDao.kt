package com.tusur.teacherhelper.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.tusur.teacherhelper.data.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subject")
    fun getAll(): Flow<List<Subject>>

    @Query("SELECT subject.* FROM subject JOIN topic ON topic.subject_id = subject.id AND topic.id = :topicId")
    suspend fun getOfTopic(topicId: Int): Subject

    @Query("SELECT * FROM subject WHERE id = :subjectId")
    suspend fun getById(subjectId: Int): Subject

    @Query("SELECT * FROM subject WHERE name = :name")
    suspend fun getByName(name: String): Subject?

    @Query("SELECT * FROM subject WHERE name LIKE :query")
    suspend fun search(query: String): List<Subject>

    @Insert
    suspend fun insert(subject: Subject): Long

    @Update
    suspend fun update(subject: Subject)

    @Transaction
    suspend fun delete(subjectId: Int) {
        deleteSubjectGroups(subjectId)
        deleteSubject(subjectId)
    }

    @Query("DELETE FROM subject_group WHERE subject_id = :subjectId")
    suspend fun deleteSubjectGroups(subjectId: Int)

    @Query("DELETE FROM subject WHERE id = :subjectId")
    suspend fun deleteSubject(subjectId: Int)
}