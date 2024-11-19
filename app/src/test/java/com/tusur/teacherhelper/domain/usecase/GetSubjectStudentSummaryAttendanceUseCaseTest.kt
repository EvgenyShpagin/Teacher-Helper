package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.SumProgress
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.Topic.Name
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetSubjectStudentSummaryAttendanceUseCaseTest {

    val studentPerformanceRepository = mockk<StudentPerformanceRepository>()
    val topicTypeRepository = mockk<TopicTypeRepository>()
    val topicRepository = mockk<TopicRepository>()

    @Test
    fun shouldReturn_1_of_1_whenWasPresent() = runTest {
        val topicIds = listOf(1)
        val studentId = 1
        val subjectId = 1

        coEvery {
            topicTypeRepository.getWithAttendance()
        } returns listOf(
            PracticeTopicType
        )

        coEvery {
            topicRepository.getOfTypes(listOf(PracticeTopicType), subjectId)
        } returns listOf(PracticeTopic)

        coEvery {
            studentPerformanceRepository.getAttendance(topicIds, studentId)
        } returns listOf(
            PracticeTopic to PerformanceItem.Attendance.Present
        )

        val useCase = GetSubjectStudentSummaryAttendanceUseCase(
            studentPerformanceRepository,
            topicTypeRepository,
            topicRepository
        )

        assertEquals(
            listOf(PracticeTopicType to SumProgress<Float>(1f, 1f)),
            useCase(studentId, subjectId, listOf(PracticeTopic.id))
        )
    }

    @Test
    fun shouldReturnEmptyList_whenNoTopicsTakenIntoAccount() = runTest {
        val studentId = 1
        val subjectId = 101
        val takenInAccountTopicIds = emptyList<Int>()

        coEvery { topicTypeRepository.getWithAttendance() } returns emptyList()
        coEvery { topicRepository.getOfTypes(emptyList(), subjectId) } returns emptyList()
        coEvery { studentPerformanceRepository.getAttendance(any(), studentId) } returns emptyList()

        val useCase = GetSubjectStudentSummaryAttendanceUseCase(
            studentPerformanceRepository,
            topicTypeRepository,
            topicRepository
        )

        assertEquals(
            emptyList<Pair<TopicType, SumProgress<Float>>>(),
            useCase(studentId, subjectId, takenInAccountTopicIds)
        )
    }

    @Test
    fun shouldReturn_3_of_5_when_2_absences() = runTest {
        val topicIds = listOf(1)
        val studentId = 1
        val subjectId = 1

        coEvery {
            topicTypeRepository.getWithAttendance()
        } returns listOf(
            PracticeTopicType
        )

        coEvery {
            topicRepository.getOfTypes(listOf(PracticeTopicType), subjectId)
        } returns listOf(PracticeTopic)

        coEvery {
            studentPerformanceRepository.getAttendance(topicIds, studentId)
        } returns listOf(
            PracticeTopic to PerformanceItem.Attendance.Present,
            PracticeTopic to PerformanceItem.Attendance.Present,
            PracticeTopic to PerformanceItem.Attendance.Excused,
            PracticeTopic to PerformanceItem.Attendance.Absent,
            PracticeTopic to PerformanceItem.Attendance.Absent,
        )

        val useCase = GetSubjectStudentSummaryAttendanceUseCase(
            studentPerformanceRepository,
            topicTypeRepository,
            topicRepository
        )

        assertEquals(
            listOf(PracticeTopicType to SumProgress<Float>(3f, 5f)),
            useCase(studentId, subjectId, listOf(PracticeTopic.id))
        )
    }

    companion object {
        val PracticeTopicType =
            TopicType(3, "Практика", "Практ", true, true, false, false, false, true)
        val PracticeTopic = Topic(
            id = 1,
            name = Name("Практика", "Практ", null, null, null),
            type = PracticeTopicType,
            deadline = null
        )
    }
}