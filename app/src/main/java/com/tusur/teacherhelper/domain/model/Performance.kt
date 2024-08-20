package com.tusur.teacherhelper.domain.model

data class Performance(
    val grade: PerformanceItem.Grade?,
    val progress: PerformanceItem.Progress?,
    val assessment: PerformanceItem.Assessment?,
    val attendance: List<PerformanceItem.Attendance>?
)
