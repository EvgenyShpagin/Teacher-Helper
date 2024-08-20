package com.tusur.teacherhelper.domain.model

data class StudentSubjectPerformance(
    val student: Student,
    val topicsWithPerformance: List<Pair<Topic, Performance>>,
)