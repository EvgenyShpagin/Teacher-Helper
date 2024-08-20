package com.tusur.teacherhelper.di

import com.tusur.teacherhelper.data.datasource.ClassTimeDataSource
import com.tusur.teacherhelper.data.repository.ClassDateRepositoryImpl
import com.tusur.teacherhelper.data.repository.ClassTimeRepositoryImpl
import com.tusur.teacherhelper.data.repository.DeadlineRepositoryImpl
import com.tusur.teacherhelper.data.repository.GroupRepositoryImpl
import com.tusur.teacherhelper.data.repository.StudentPerformanceRepositoryImpl
import com.tusur.teacherhelper.data.repository.StudentRepositoryImpl
import com.tusur.teacherhelper.data.repository.SubjectGroupRepositoryImpl
import com.tusur.teacherhelper.data.repository.SubjectRepositoryImpl
import com.tusur.teacherhelper.data.repository.TopicRepositoryImpl
import com.tusur.teacherhelper.data.repository.TopicTypeRepositoryImpl
import com.tusur.teacherhelper.data.room.db.AppDatabase

class AppModuleImpl(database: AppDatabase) : AppModule {
    override val groupRepository = GroupRepositoryImpl(
        database.getGroupDao(),
        database.getStudentDao(),
        database.getStudentPerformanceDao()
    )
    override val studentPerformanceRepository = StudentPerformanceRepositoryImpl(
        database.getStudentPerformanceDao(),
        database.getTopicDao(),
        database.getTopicTypeDao(),
        database.getDeadlineDao(),
        database.getClassDateDao(),
        database.getStudentDao(),
    )
    override val studentRepository = StudentRepositoryImpl(database.getStudentDao())
    override val subjectGroupRepository =
        SubjectGroupRepositoryImpl(database.getSubjectGroupDao(), database.getGroupDao())
    override val subjectRepository = SubjectRepositoryImpl(database.getSubjectDao())
    override val classDateRepository = ClassDateRepositoryImpl(database.getClassDateDao())
    override val topicRepository = TopicRepositoryImpl(
        database.getTopicDao(),
        database.getDeadlineDao(),
        database.getTopicTypeDao()
    )
    override val topicTypeRepository = TopicTypeRepositoryImpl(database.getTopicTypeDao())
    override val deadlineRepository = DeadlineRepositoryImpl(database.getDeadlineDao())

    override val classTimeRepository = ClassTimeRepositoryImpl(ClassTimeDataSource())
}