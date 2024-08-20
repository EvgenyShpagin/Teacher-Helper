package com.tusur.teacherhelper.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_date")
data class ClassDate(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "datetime_millis")
    val datetimeMillis: Long
)