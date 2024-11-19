package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetSubjectStudentPerformanceUseCaseTest {
    val studentPerformanceRepository = mockk<StudentPerformanceRepository>()
    val topicRepository = mockk<TopicRepository>()

    @Test
    fun shouldReturnSamePerformanceAsRepository_whenAllTopicsWithPerformance() = runTest {
        // Lab1 -- 100%, others -- 0%
        val labToPerformanceList = Labs.mapIndexed { index, topic ->
            val progress = PerformanceItem.Progress(if (index == 0) 1f else 0f)
            topic to Performance(null, progress, null, listOf(PerformanceItem.Attendance.Present))
        }

        coEvery {
            studentPerformanceRepository.getFinalPerformance(
                topicIds = Labs.map { it.id },
                studentId = 1
            )
        } returns flow { emit(labToPerformanceList) }

        coEvery {
            topicRepository.getIdsBySubject(subjectId = 1, withCancelled = false)
        } returns Labs.map { it.id }

        val useCase = GetSubjectStudentPerformanceUseCase(
            studentPerformanceRepository, topicRepository
        )
        assertEquals(
            labToPerformanceList,
            useCase(studentId = 1, subjectId = 1).first()
        )
    }

    @Test
    fun shouldExtendResultListWithEmptyPerformance_whenSomeTopicsWithoutPerformance() = runTest {
        val topics = Labs
        val topicsWithoutPerformance = Labs.map { it.copy(id = it.id + 10) }
        val allTopics = topics + topicsWithoutPerformance

        // Lab1 -- 100%, others -- 0%
        val labToPerformanceList = topics.mapIndexed { index, topic ->
            val progress = PerformanceItem.Progress(if (index == 0) 1f else 0f)
            topic to Performance(null, progress, null, listOf(PerformanceItem.Attendance.Present))
        }

        coEvery {
            studentPerformanceRepository.getFinalPerformance(
                topicIds = allTopics.map { it.id },
                studentId = 1
            )
        } returns flow { emit(labToPerformanceList) }

        // Calls of getById are being made only for topics without performance
        topicsWithoutPerformance.forEach { topic ->
            coEvery { topicRepository.getById(topic.id) } returns topic
        }

        // Consider that there are some topics without performance
        coEvery {
            topicRepository.getIdsBySubject(subjectId = 1, withCancelled = false)
        } returns allTopics.map { it.id }

        val useCase = GetSubjectStudentPerformanceUseCase(
            studentPerformanceRepository, topicRepository
        )

        val expectedList =
            labToPerformanceList + topicsWithoutPerformance.map { topic ->
                topic to Performance(null, PerformanceItem.Progress(0f), null, emptyList())
            }

        assertEquals(
            expectedList,
            useCase(studentId = 1, subjectId = 1).first()
        )
    }

    companion object {
        val LabTopicType = TopicType(1, "Лабораторная", "", true, false, true, false, true, false)
        val Labs = List(4) { i ->
            Topic(
                id = i,
                type = LabTopicType,
                name = Topic.Name("Лабораторная", "", null, null, null)
            )
        }
    }
}