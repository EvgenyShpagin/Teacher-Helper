package com.tusur.teacherhelper.di

import com.tusur.teacherhelper.data.repository.ClassDateRepositoryImpl
import com.tusur.teacherhelper.data.repository.ClassTimeRepositoryImpl
import com.tusur.teacherhelper.data.repository.DeadlineRepositoryImpl
import com.tusur.teacherhelper.data.repository.FakeGroupRepository
import com.tusur.teacherhelper.data.repository.StudentPerformanceRepositoryImpl
import com.tusur.teacherhelper.data.repository.StudentRepositoryImpl
import com.tusur.teacherhelper.data.repository.SubjectGroupRepositoryImpl
import com.tusur.teacherhelper.data.repository.SubjectRepositoryImpl
import com.tusur.teacherhelper.data.repository.TopicRepositoryImpl
import com.tusur.teacherhelper.data.repository.TopicTypeRepositoryImpl
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
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class RepositoryTestModule {

    @Singleton
    @Binds
    abstract fun bindGroupRepository(fakeImpl: FakeGroupRepository): GroupRepository


    // Keep release implementation to other repositories for now

    @Binds
    abstract fun bindClassDateRepository(
        impl: ClassDateRepositoryImpl
    ): ClassDateRepository

    @Binds
    abstract fun bindClassTimeRepository(
        impl: ClassTimeRepositoryImpl
    ): ClassTimeRepository

    @Binds
    abstract fun bindDeadlineRepository(
        impl: DeadlineRepositoryImpl
    ): DeadlineRepository

    @Binds
    abstract fun bindStudentPerformanceRepository(
        impl: StudentPerformanceRepositoryImpl
    ): StudentPerformanceRepository

    @Binds
    abstract fun bindStudentRepository(
        impl: StudentRepositoryImpl
    ): StudentRepository

    @Binds
    abstract fun bindSubjectGroupRepository(
        impl: SubjectGroupRepositoryImpl
    ): SubjectGroupRepository

    @Binds
    abstract fun bindSubjectRepository(
        impl: SubjectRepositoryImpl
    ): SubjectRepository

    @Binds
    abstract fun bindTopicRepository(
        impl: TopicRepositoryImpl
    ): TopicRepository

    @Binds
    abstract fun bindTopicTypeRepository(
        impl: TopicTypeRepositoryImpl
    ): TopicTypeRepository
}