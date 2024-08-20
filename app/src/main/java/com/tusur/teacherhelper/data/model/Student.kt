package com.tusur.teacherhelper.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.tusur.teacherhelper.domain.model.Student.Name

@Entity(
    tableName = "student",
    foreignKeys = [ForeignKey(Group::class, ["id"], ["group_id"])]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: Name,
    @ColumnInfo(name = "group_id")
    val groupId: Int
)