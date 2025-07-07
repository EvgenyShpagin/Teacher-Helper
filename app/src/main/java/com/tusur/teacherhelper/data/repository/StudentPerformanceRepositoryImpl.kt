package com.tusur.teacherhelper.data.repository

import com.tusur.teacherhelper.data.model.StudentTopicPerformance
import com.tusur.teacherhelper.data.room.dao.ClassDateDao
import com.tusur.teacherhelper.data.room.dao.DeadlineDao
import com.tusur.teacherhelper.data.room.dao.StudentDao
import com.tusur.teacherhelper.data.room.dao.StudentTopicPerformanceDao
import com.tusur.teacherhelper.data.room.dao.TopicDao
import com.tusur.teacherhelper.data.room.dao.TopicTypeDao
import com.tusur.teacherhelper.domain.model.Datetime
import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import com.tusur.teacherhelper.domain.util.applyOrderOf
import com.tusur.teacherhelper.presentation.core.util.toNativeArray
import com.tusur.teacherhelper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudentPerformanceRepositoryImpl @Inject constructor(
    private val studentTopicPerformanceDao: StudentTopicPerformanceDao,
    private val topicDao: TopicDao,
    private val topicTypeDao: TopicTypeDao,
    private val deadlineDao: DeadlineDao,
    private val classDateDao: ClassDateDao,
    private val studentDao: StudentDao
) : StudentPerformanceRepository {

    override suspend fun getSetPerformance(
        topicId: Int,
        studentId: Int,
        classDateId: Int
    ): Performance? {
        return studentTopicPerformanceDao.get(topicId, studentId, classDateId)?.toDomain()
    }

    override suspend fun getSetPerformance(
        topicId: Int,
        students: List<Student>,
        classDateId: Int
    ): Flow<List<Pair<Student, Performance>>> {
        val studentIds = students.map { it.id }
        return studentTopicPerformanceDao.getAll(topicId, studentIds.toNativeArray(), classDateId)
            .map { list ->
                // Restore order of students
                list.applyOrderOf(students) { performance, student ->
                    performance.studentId == student.id
                }.map { (dataPerformance, student) -> student to dataPerformance.toDomain() }
            }
    }

    override suspend fun getSetPerformance(
        topicIds: List<Int>,
        studentId: Int
    ): List<Pair<Topic, Performance>> {
        return studentTopicPerformanceDao.getOfStudent(topicIds, studentId).map {
            getTopic(it.topicId) to it.toDomain()
        }
    }

    override suspend fun getFinalPerformance(
        topicIds: List<Int>,
        studentId: Int
    ): Flow<List<Pair<Topic, Performance>>> {
        return studentTopicPerformanceDao.getFinalPerformance(topicIds, studentId).map { list ->
            list.map { getTopic(it.topicId) to it.toDomain() }
        }
    }

    override suspend fun getAttendance(
        topicIds: List<Int>,
        studentId: Int
    ): List<Pair<Topic, PerformanceItem.Attendance?>> {
        return studentTopicPerformanceDao.getOfStudent(topicIds, studentId).map {
            getTopic(it.topicId) to it.attendance
        }
    }


    override suspend fun update(
        studentId: Int,
        topicId: Int,
        classDateId: Int,
        performance: Performance
    ) {
        studentTopicPerformanceDao.update(
            StudentTopicPerformance(
                studentId,
                topicId,
                classDateId,
                performance.grade,
                performance.progress,
                performance.attendance?.single(),
                performance.assessment
            )
        )
    }

    override suspend fun add(
        studentId: Int,
        topicId: Int,
        classDateId: Int,
        performance: Performance
    ) {
        studentTopicPerformanceDao.insert(
            studentId,
            topicId,
            classDateId,
            performance.grade,
            performance.progress,
            performance.attendance?.single(),
            performance.assessment
        )
    }

    override suspend fun deletePerformance(
        topicId: Int,
        groupListIds: List<Int>,
        datetime: List<Datetime>
    ) {
        val classDateIds = datetime.map { classDateDao.getByDatetimeMillis(it.toMillis())!!.id }
        val studentIds = ArrayList<Int>()
        groupListIds.forEach { groupId ->
            studentIds.addAll(studentDao.getAllIds(groupId))
        }
        studentTopicPerformanceDao.delete(topicId, studentIds, classDateIds)
    }

    override suspend fun deleteAllTopic(topicId: Int) {
        studentTopicPerformanceDao.deleteAllTopic(topicId)
    }

    override suspend fun getFinalPerformanceClassDayDatetimeMs(
        studentId: Int,
        topicId: Int
    ): Long? {
        return studentTopicPerformanceDao.getFinalPerformance(
            topicIds = listOf(topicId),
            studentId = studentId
        ).first().singleOrNull()?.let {
            classDateDao.getById(it.classDateId)?.datetimeMillis
        }
    }

    private suspend fun getTopic(topicId: Int): Topic {
        val topic = topicDao.get(topicId)!!
        val topicType = topicTypeDao.get(topic.topicTypeId)!!
        val deadline = deadlineDao.getOfTopic(topic.id)
        return topic.toDomain(topicType.toDomain(), deadline?.toDomain())
    }
}