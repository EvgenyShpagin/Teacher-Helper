package com.tusur.teacherhelper.di

import android.content.Context
import androidx.room.Room
import com.tusur.teacherhelper.data.room.dao.ClassDateDao
import com.tusur.teacherhelper.data.room.dao.DeadlineDao
import com.tusur.teacherhelper.data.room.dao.GroupDao
import com.tusur.teacherhelper.data.room.dao.StudentDao
import com.tusur.teacherhelper.data.room.dao.StudentTopicPerformanceDao
import com.tusur.teacherhelper.data.room.dao.SubjectDao
import com.tusur.teacherhelper.data.room.dao.SubjectGroupDao
import com.tusur.teacherhelper.data.room.dao.TopicDao
import com.tusur.teacherhelper.data.room.dao.TopicTypeDao
import com.tusur.teacherhelper.data.room.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


private const val DATABASE_NAME = "teacher-helper"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        ).createFromAsset("student_performance.db").build()
    }

    @Provides
    fun provideClassDateDao(database: AppDatabase): ClassDateDao {
        return database.getClassDateDao()
    }

    @Provides
    fun provideDeadlineDao(database: AppDatabase): DeadlineDao {
        return database.getDeadlineDao()
    }

    @Provides
    fun provideGroupDao(database: AppDatabase): GroupDao {
        return database.getGroupDao()
    }

    @Provides
    fun provideStudentDao(database: AppDatabase): StudentDao {
        return database.getStudentDao()
    }

    @Provides
    fun provideStudentTopicPerformanceDao(database: AppDatabase): StudentTopicPerformanceDao {
        return database.getStudentPerformanceDao()
    }

    @Provides
    fun provideSubjectDao(database: AppDatabase): SubjectDao {
        return database.getSubjectDao()
    }

    @Provides
    fun provideSubjectGroupDao(database: AppDatabase): SubjectGroupDao {
        return database.getSubjectGroupDao()
    }

    @Provides
    fun provideTopicDao(database: AppDatabase): TopicDao {
        return database.getTopicDao()
    }

    @Provides
    fun provideTopicTypeDao(database: AppDatabase): TopicTypeDao {
        return database.getTopicTypeDao()
    }
}