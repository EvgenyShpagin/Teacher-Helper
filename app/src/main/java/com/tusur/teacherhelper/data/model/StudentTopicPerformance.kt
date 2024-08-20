package com.tusur.teacherhelper.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.tusur.teacherhelper.domain.model.PerformanceItem

@Entity(
    tableName = "student_topic_performance",
    primaryKeys = ["student_id", "topic_id", "class_date_id"],
    foreignKeys = [
        ForeignKey(
            Student::class,
            ["id"],
            ["student_id"]
        ),
        ForeignKey(
            Topic::class,
            ["id"],
            ["topic_id"]
        ),
        ForeignKey(
            ClassDate::class,
            ["id"],
            ["class_date_id"]
        )
    ]
)
data class StudentTopicPerformance(
    @ColumnInfo(name = "student_id")
    val studentId: Int,
    @ColumnInfo(name = "topic_id")
    val topicId: Int,
    @ColumnInfo(name = "class_date_id")
    val classDateId: Int,
    val grade: PerformanceItem.Grade?,
    val progress: PerformanceItem.Progress?,
    val attendance: PerformanceItem.Attendance?,
    val assessment: PerformanceItem.Assessment?
)