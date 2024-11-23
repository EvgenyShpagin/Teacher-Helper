package com.tusur.teacherhelper.data.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tusur.teacherhelper.data.model.ClassDate
import com.tusur.teacherhelper.data.model.Deadline
import com.tusur.teacherhelper.data.model.Group
import com.tusur.teacherhelper.data.model.Student
import com.tusur.teacherhelper.data.model.StudentTopicPerformance
import com.tusur.teacherhelper.data.model.Subject
import com.tusur.teacherhelper.data.model.SubjectGroup
import com.tusur.teacherhelper.data.model.Topic
import com.tusur.teacherhelper.data.model.TopicType
import com.tusur.teacherhelper.data.room.Converters
import com.tusur.teacherhelper.data.room.dao.ClassDateDao
import com.tusur.teacherhelper.data.room.dao.DeadlineDao
import com.tusur.teacherhelper.data.room.dao.GroupDao
import com.tusur.teacherhelper.data.room.dao.StudentDao
import com.tusur.teacherhelper.data.room.dao.StudentTopicPerformanceDao
import com.tusur.teacherhelper.data.room.dao.SubjectDao
import com.tusur.teacherhelper.data.room.dao.SubjectGroupDao
import com.tusur.teacherhelper.data.room.dao.TopicDao
import com.tusur.teacherhelper.data.room.dao.TopicTypeDao


@Database(
    entities = [
        ClassDate::class,
        Deadline::class,
        Group::class,
        Student::class,
        StudentTopicPerformance::class,
        Subject::class,
        SubjectGroup::class,
        Topic::class,
        TopicType::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getGroupDao(): GroupDao
    abstract fun getStudentDao(): StudentDao
    abstract fun getSubjectDao(): SubjectDao
    abstract fun getClassDateDao(): ClassDateDao
    abstract fun getDeadlineDao(): DeadlineDao
    abstract fun getSubjectGroupDao(): SubjectGroupDao
    abstract fun getTopicDao(): TopicDao
    abstract fun getTopicTypeDao(): TopicTypeDao
    abstract fun getStudentPerformanceDao(): StudentTopicPerformanceDao

}