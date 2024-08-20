package com.tusur.teacherhelper.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tusur.teacherhelper.data.model.Student
import com.tusur.teacherhelper.domain.model.Student.Name
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM student WHERE student.id = :studentId")
    suspend fun get(studentId: Int): Student?

    @Query("SELECT * FROM student WHERE student.group_id = :groupId")
    suspend fun getAll(groupId: Int): List<Student>

    @Query("SELECT * FROM student WHERE student.group_id = :groupId")
    fun getAllAsFlow(groupId: Int): Flow<List<Student>>

    @Query("SELECT id FROM student WHERE group_id = :groupId")
    suspend fun getAllIds(groupId: Int): List<Int>

    @Query("SELECT * FROM student WHERE student.name = :name")
    suspend fun getByName(name: Name): Student?

    @Query("SELECT * FROM student WHERE group_id = :groupId AND name LIKE :nameQuery")
    suspend fun searchByFullName(nameQuery: String, groupId: Int): List<Student>

    @Insert
    suspend fun insert(student: Student): Long

    @Insert
    suspend fun insert(students: List<Student>)

    @Update
    suspend fun update(student: Student)

    @Query("DELETE FROM student WHERE id = :studentId")
    suspend fun delete(studentId: Int)

    suspend fun delete(student: Student) = delete(student.id)
}