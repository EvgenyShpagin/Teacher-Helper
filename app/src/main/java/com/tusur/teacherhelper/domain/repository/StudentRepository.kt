package com.tusur.teacherhelper.domain.repository

import com.tusur.teacherhelper.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    suspend fun getAll(groupId: Int): List<Student>
    fun getAllAsFlow(groupId: Int): Flow<List<Student>>
    suspend fun getAllIds(groupId: Int): List<Int>
    suspend fun getById(studentId: Int): Student?
    suspend fun getByName(name: Student.Name): Student?
    suspend fun add(student: Student, groupId: Int): Int
    suspend fun add(students: List<Student>, groupId: Int)
    suspend fun update(student: Student, groupId: Int)
    suspend fun searchByName(nameQuery: String, groupId: Int): List<Student>
    suspend fun delete(studentId: Int)
}