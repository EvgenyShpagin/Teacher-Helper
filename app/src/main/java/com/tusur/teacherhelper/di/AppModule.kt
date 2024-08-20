package com.tusur.teacherhelper.di

import com.tusur.teacherhelper.domain.repository.ClassDateRepository
import com.tusur.teacherhelper.domain.repository.ClassTimeRepository
import com.tusur.teacherhelper.domain.repository.DeadlineRepository
import com.tusur.teacherhelper.domain.repository.GroupRepository
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import com.tusur.teacherhelper.domain.repository.StudentRepository
import com.tusur.teacherhelper.domain.repository.SubjectGroupRepository
import com.tusur.teacherhelper.domain.repository.SubjectRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository

interface AppModule {
    val groupRepository: GroupRepository
    val studentPerformanceRepository: StudentPerformanceRepository
    val studentRepository: StudentRepository
    val subjectGroupRepository: SubjectGroupRepository
    val subjectRepository: SubjectRepository
    val classDateRepository: ClassDateRepository
    val topicRepository: TopicRepository
    val topicTypeRepository: TopicTypeRepository
    val deadlineRepository: DeadlineRepository
    val classTimeRepository: ClassTimeRepository
}