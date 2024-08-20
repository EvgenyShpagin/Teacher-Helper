package com.tusur.teacherhelper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group")
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val number: String
)