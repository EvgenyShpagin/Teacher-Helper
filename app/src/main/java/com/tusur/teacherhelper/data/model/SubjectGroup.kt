package com.tusur.teacherhelper.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "subject_group",
    primaryKeys = ["subject_id", "group_id"],
    foreignKeys = [
        ForeignKey(Subject::class, ["id"], ["subject_id"]),
        ForeignKey(Group::class, ["id"], ["group_id"])
    ]
)
data class SubjectGroup(
    @ColumnInfo(name = "subject_id")
    val subjectId: Int,
    @ColumnInfo(name = "group_id")
    val groupId: Int
)