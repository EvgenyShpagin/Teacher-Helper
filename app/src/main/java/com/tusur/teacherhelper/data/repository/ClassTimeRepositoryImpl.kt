package com.tusur.teacherhelper.data.repository

import com.tusur.teacherhelper.data.datasource.ClassTimeDataSource
import com.tusur.teacherhelper.domain.model.ClassTime
import com.tusur.teacherhelper.domain.repository.ClassTimeRepository

class ClassTimeRepositoryImpl(
    private val classTimeDataSource: ClassTimeDataSource
) : ClassTimeRepository {

    override fun getAll(): List<ClassTime> {
        return classTimeDataSource.getAll()
    }
}