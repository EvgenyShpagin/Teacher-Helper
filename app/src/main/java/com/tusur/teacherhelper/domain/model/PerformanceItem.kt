package com.tusur.teacherhelper.domain.model

import androidx.annotation.FloatRange
import androidx.annotation.IntRange

sealed class PerformanceItem {

    data class Grade(@IntRange(0, 5) val value: Int) : PerformanceItem()

    data class Progress(@FloatRange(from = 0.0, to = 1.0) val value: Float) : PerformanceItem()

    sealed class Assessment : PerformanceItem() {
        data object PASS : Assessment()
        data object FAIL : Assessment()
    }

    sealed class Attendance : PerformanceItem() {
        data object Present : Attendance()
        data object Absent : Attendance() // отсутствие
        data object Excused : Attendance() // отсутствие по уважительной причине
    }
}