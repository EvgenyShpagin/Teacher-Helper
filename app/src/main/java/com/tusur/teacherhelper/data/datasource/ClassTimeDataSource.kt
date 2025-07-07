package com.tusur.teacherhelper.data.datasource

import com.tusur.teacherhelper.domain.model.ClassTime
import kotlinx.datetime.LocalTime
import javax.inject.Inject

class ClassTimeDataSource @Inject constructor() {
    fun getAll(): List<ClassTime> {
        return classTimeList
    }

    private companion object {
        val classTimeList = listOf(
            ClassTime(initTime = LocalTime(8, 50), finishTime = LocalTime(10, 25)),
            ClassTime(initTime = LocalTime(10, 40), finishTime = LocalTime(12, 15)),
            ClassTime(initTime = LocalTime(13, 15), finishTime = LocalTime(14, 50)),
            ClassTime(initTime = LocalTime(15, 0), finishTime = LocalTime(16, 35)),
            ClassTime(initTime = LocalTime(16, 45), finishTime = LocalTime(18, 20)),
            ClassTime(initTime = LocalTime(18, 30), finishTime = LocalTime(20, 5)),
            ClassTime(initTime = LocalTime(20, 15), finishTime = LocalTime(21, 50)),
        )
    }
}