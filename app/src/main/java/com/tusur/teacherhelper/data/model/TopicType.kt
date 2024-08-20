package com.tusur.teacherhelper.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topic_type")
data class TopicType(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    @ColumnInfo(name = "short_name")
    val shortName: String,
    @ColumnInfo(name = "deadline_allowed")
    val canDeadlineBeSpecified: Boolean,
    @ColumnInfo(name = "grade_acceptable")
    val isGradeAcceptable: Boolean,
    @ColumnInfo(name = "progress_acceptable")
    val isProgressAcceptable: Boolean,
    @ColumnInfo(name = "assessment_acceptable")
    val isAssessmentAcceptable: Boolean,
    @ColumnInfo(name = "attendance_acceptable")
    val isAttendanceAcceptable: Boolean,
    @ColumnInfo(name = "attendance_one_class_only")
    val isAttendanceForOneClassOnly: Boolean
)