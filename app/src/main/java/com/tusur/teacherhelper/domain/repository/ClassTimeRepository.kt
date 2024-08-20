package com.tusur.teacherhelper.domain.repository

import com.tusur.teacherhelper.domain.model.ClassTime

interface ClassTimeRepository {
    fun getAll(): List<ClassTime>
}