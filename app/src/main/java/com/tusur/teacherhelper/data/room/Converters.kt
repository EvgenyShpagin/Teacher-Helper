package com.tusur.teacherhelper.data.room

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.Topic

@TypeConverters
class Converters {
    @TypeConverter
    fun Topic.Name.convertToString(): String {
        return Gson().toJson(this)
    }

    @TypeConverter
    fun String.convertToTopicName(): Topic.Name {
        return Gson().fromJson(this, Topic.Name::class.java)
    }

    @TypeConverter
    fun Student.Name.convertToString(): String {
        return full
    }

    @TypeConverter
    fun String.convertToName(): Student.Name {
        val names = split(' ')
        return Student.Name(names[0], names[1], names.getOrElse(2) { "" })
    }

    @TypeConverter
    fun PerformanceItem.Grade?.convertToInt(): Int? {
        return this?.value
    }

    @TypeConverter
    fun Int?.convertToGrade(): PerformanceItem.Grade? {
        return this?.let { PerformanceItem.Grade(it) }
    }

    @TypeConverter
    fun PerformanceItem.Progress?.convertToFloat(): Float? {
        return this?.value
    }

    @TypeConverter
    fun Float?.convertToProgress(): PerformanceItem.Progress? {
        return this?.let { PerformanceItem.Progress(it) }
    }

    @TypeConverter
    fun PerformanceItem.Attendance?.convertToInt(): Int? {
        return when (this) {
            null -> null
            PerformanceItem.Attendance.Absent -> 0
            PerformanceItem.Attendance.Excused -> 1
            PerformanceItem.Attendance.Present -> 2
        }
    }

    @TypeConverter
    fun Int?.convertToAttendance(): PerformanceItem.Attendance? {
        return when (this) {
            null -> null
            0 -> PerformanceItem.Attendance.Absent
            1 -> PerformanceItem.Attendance.Excused
            else -> PerformanceItem.Attendance.Present
        }
    }

    @TypeConverter
    fun PerformanceItem.Assessment?.convertToInt(): Int? {
        return when (this) {
            null -> null
            PerformanceItem.Assessment.FAIL -> 0
            PerformanceItem.Assessment.PASS -> 1
        }
    }

    @TypeConverter
    fun Int?.convertToAssessment(): PerformanceItem.Assessment? {
        return when (this) {
            null -> null
            0 -> PerformanceItem.Assessment.FAIL
            else -> PerformanceItem.Assessment.PASS
        }
    }
}
