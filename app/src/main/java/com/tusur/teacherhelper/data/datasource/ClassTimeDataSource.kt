package com.tusur.teacherhelper.data.datasource

import com.tusur.teacherhelper.domain.model.ClassTime
import com.tusur.teacherhelper.domain.model.Time
import javax.inject.Inject

class ClassTimeDataSource @Inject constructor() {
    fun getAll(): List<ClassTime> {
        return classTimeList
    }

    private companion object {
        val classTimeList = listOf(
            ClassTime(initTime = Time(8, 50), finishTime = Time(10, 25)),
            ClassTime(initTime = Time(10, 40), finishTime = Time(12, 15)),
            ClassTime(initTime = Time(13, 15), finishTime = Time(14, 50)),
            ClassTime(initTime = Time(15, 0), finishTime = Time(16, 35)),
            ClassTime(initTime = Time(16, 45), finishTime = Time(18, 20)),
            ClassTime(initTime = Time(18, 30), finishTime = Time(20, 5)),
            ClassTime(initTime = Time(20, 15), finishTime = Time(21, 50)),
        )
    }
}