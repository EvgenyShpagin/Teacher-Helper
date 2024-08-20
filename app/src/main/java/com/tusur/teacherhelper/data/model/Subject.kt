package com.tusur.teacherhelper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subject")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String
)