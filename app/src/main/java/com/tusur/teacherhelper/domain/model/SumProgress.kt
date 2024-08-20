package com.tusur.teacherhelper.domain.model

data class SumProgress<out T : Number>(
    val reached: T,
    val total: T
)