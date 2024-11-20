package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.SumProgress
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.TopicType
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetSubjectStudentSummaryPerformanceUseCaseTest {

    val getTotalStudentPerformance = mockk<GetSubjectStudentPerformanceUseCase>()

    // Use real implementation for simplicity
    val getSuggestedProgressForGrade = GetSuggestedProgressForGradeUseCase()

    @Test
    fun shouldReturn_0_performance_whenAllLabsFailedToPass() = runTest {
        val studentId = 1
        val subjectId = 1

        val studentTotalPerformance = Labs.map { lab ->
            lab to Performance(
                null, PerformanceItem.Progress(0f),
                null, listOf(PerformanceItem.Attendance.Present)
            )
        }

        coEvery {
            getTotalStudentPerformance(studentId, subjectId)
        } returns flow { emit(studentTotalPerformance) }

        val useCase = GetSubjectStudentSummaryPerformanceUseCase(
            getTotalStudentPerformance,
            getSuggestedProgressForGrade
        )

        val expectedTypeToPerformanceList = listOf(
            LabTopicType to SumProgress(0f, 4f)
        )

        assertEquals(
            expectedTypeToPerformanceList,
            useCase(studentId, subjectId, Labs.map { it.id })
        )
    }

    @Test
    fun shouldReturn_100_performance_whenAllLabsPassed() = runTest {
        val studentId = 1
        val subjectId = 1

        val studentTotalPerformance = Labs.map { lab ->
            lab to Performance(
                null, PerformanceItem.Progress(1f),
                null, listOf(PerformanceItem.Attendance.Present)
            )
        }

        coEvery {
            getTotalStudentPerformance(studentId, subjectId)
        } returns flow { emit(studentTotalPerformance) }

        val useCase = GetSubjectStudentSummaryPerformanceUseCase(
            getTotalStudentPerformance,
            getSuggestedProgressForGrade
        )

        val expectedTypeToPerformanceList = listOf(
            LabTopicType to SumProgress(4f, 4f)
        )

        assertEquals(
            expectedTypeToPerformanceList,
            useCase(studentId, subjectId, Labs.map { it.id })
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