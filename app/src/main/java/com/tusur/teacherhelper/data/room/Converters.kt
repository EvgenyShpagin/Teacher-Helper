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
    fun convertToString(name: Topic.Name): String {
        return Gson().toJson(name)
    }

    @TypeConverter
    fun convertToTopicName(string: String): Topic.Name {
        return Gson().fromJson(string, Topic.Name::class.java)
    }

    @TypeConverter
    fun convertToString(name: Student.Name): String {
        return name.full
    }

    @TypeConverter
    fun convertToName(string: String): Student.Name {
        val names = string.split(' ')
        return Student.Name(names[0], names[1], names.getOrElse(2) { "" })
    }

    @TypeConverter
    fun convertToInt(grade: PerformanceItem.Grade?): Int? {
        return grade?.value
    }

    @TypeConverter
    fun convertToGrade(grade: Int?): PerformanceItem.Grade? {
        return grade?.let { PerformanceItem.Grade(it) }
    }

    @TypeConverter
    fun convertToFloat(progress: PerformanceItem.Progress?): Float? {
        return progress?.value
    }

    @TypeConverter
    fun convertToProgress(progress: Float?): PerformanceItem.Progress? {
        return progress?.let { PerformanceItem.Progress(it) }
    }

    @TypeConverter
    fun convertToInt(attendance: PerformanceItem.Attendance?): Int? {
        return when (attendance) {
            null -> null
            PerformanceItem.Attendance.Absent -> 0
            PerformanceItem.Attendance.Excused -> 1
            PerformanceItem.Attendance.Present -> 2
        }
    }

    @TypeConverter
    fun convertToAttendance(ordinal: Int?): PerformanceItem.Attendance? {
        return when (ordinal) {
            null -> null
            0 -> PerformanceItem.Attendance.Absent
            1 -> PerformanceItem.Attendance.Excused
            else -> PerformanceItem.Attendance.Present
        }
    }

    @TypeConverter
    fun convertToInt(assessment: PerformanceItem.Assessment?): Int? {
        return when (assessment) {
            null -> null
            PerformanceItem.Assessment.FAIL -> 0
            PerformanceItem.Assessment.PASS -> 1
        }
    }

    @TypeConverter
    fun convertToAssessment(ordinal: Int?): PerformanceItem.Assessment? {
        return when (ordinal) {
            null -> null
            0 -> PerformanceItem.Assessment.FAIL
            else -> PerformanceItem.Assessment.PASS
        }
    }
}
