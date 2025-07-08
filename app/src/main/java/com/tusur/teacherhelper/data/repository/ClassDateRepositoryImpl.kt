package com.tusur.teacherhelper.data.repository

import com.tusur.teacherhelper.data.model.ClassDate
import com.tusur.teacherhelper.data.room.dao.ClassDateDao
import com.tusur.teacherhelper.domain.repository.ClassDateRepository
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.domain.util.toEpochMillis
import com.tusur.teacherhelper.toDomain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class ClassDateRepositoryImpl @Inject constructor(
    private val classDataDao: ClassDateDao
) : ClassDateRepository {

    override suspend fun update(topicId: Int, oldDatetimeMs: Long, newDatetimeMs: Long) {
        val updatedClassDate = classDataDao.getByDatetimeMillis(oldDatetimeMs)!!
            .copy(datetimeMillis = newDatetimeMs)
        classDataDao.update(updatedClassDate)
    }

    override suspend fun getShared(topicId: Int, groupsIds: List<Int>): List<LocalDateTime> {
        return classDataDao.getShared(topicId, groupsIds).map { it.toDomain() }
    }

    override suspend fun getOfTopic(topicId: Int): List<LocalDateTime> {
        return classDataDao.getOfTopic(topicId).map { it.toDomain() }
    }

    override suspend fun getId(date: LocalDate): Int? {
        return getIdByMillis(date.toEpochMillis())
    }

    override suspend fun getIdByMillis(datetimeMillis: Long): Int? {
        return classDataDao.getByDatetimeMillis(datetimeMillis)?.id
    }

    override suspend fun add(datetimeMillis: Long): Int {
        return classDataDao.insert(ClassDate(NO_ID, datetimeMillis)).toInt()
    }

    override suspend fun delete(datetimeMillis: Long) {
        classDataDao.delete(datetimeMillis)
    }
}
