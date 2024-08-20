package com.tusur.teacherhelper.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.tusur.teacherhelper.domain.model.Topic.Name

@Entity(
    tableName = "topic",
    foreignKeys = [
        ForeignKey(Subject::class, ["id"], ["subject_id"]),
        ForeignKey(TopicType::class, ["id"], ["topic_type_id"]),
        ForeignKey(Deadline::class, ["id"], ["deadline_id"])
    ]
)
data class Topic(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: Name,
    @ColumnInfo(name = "subject_id")
    val subjectId: Int,
    @ColumnInfo(name = "topic_type_id")
    val topicTypeId: Int,
    @ColumnInfo(name = "deadline_id")
    val deadlineId: Int?,
    @ColumnInfo(name = "cancelled")
    val isCancelled: Boolean
)