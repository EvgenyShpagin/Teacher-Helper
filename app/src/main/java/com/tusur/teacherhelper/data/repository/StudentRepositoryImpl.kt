package com.tusur.teacherhelper.data.repository

import com.tusur.teacherhelper.data.room.dao.StudentDao
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.repository.StudentRepository
import com.tusur.teacherhelper.toData
import com.tusur.teacherhelper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StudentRepositoryImpl(private val studentDao: StudentDao) : StudentRepository {
    override suspend fun getAll(groupId: Int): List<Student> {
        return studentDao.getAll(groupId).map { it.toDomain() }
    }

    override fun getAllAsFlow(groupId: Int): Flow<List<Student>> {
        return studentDao.getAllAsFlow(groupId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAllIds(groupId: Int): List<Int> {
        return studentDao.getAllIds(groupId).map { it }
    }

    override suspend fun getById(studentId: Int): Student? {
        return studentDao.get(studentId)?.toDomain()
    }

    override suspend fun getByName(name: Student.Name): Student? {
        return studentDao.getByName(name)?.toDomain()
    }

    override suspend fun add(student: Student, groupId: Int): Int {
        return studentDao.insert(student.toData(groupId)).toInt()
    }

    override suspend fun add(students: List<Student>, groupId: Int) {
        studentDao.insert(students.map { it.toData(groupId) })
    }

    override suspend fun update(student: Student, groupId: Int) {
        studentDao.update(student.toData(groupId))
    }

    override suspend fun searchByName(nameQuery: String, groupId: Int): List<Student> {
        return studentDao.searchByFullName(nameQuery, groupId).map { it.toDomain() }
    }

    override suspend fun delete(studentId: Int) {
        studentDao.delete(studentId)
    }
}