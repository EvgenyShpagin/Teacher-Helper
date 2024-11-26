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

    val studentId = 1
    val subjectId = 1

    val getTotalStudentPerformance = mockk<GetSubjectStudentPerformanceUseCase>()

    // Use real implementations for simplicity
    val getSuggestedProgressForGrade = GetSuggestedProgressForGradeUseCase()
    val getSuggestedProgressForAssessment = GetSuggestedProgressForAssessmentUseCase()

    @Test
    fun shouldReturn_0_performance_whenAllLabsFailedToPass() = runTest {
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
            getSuggestedProgressForGrade,
            getSuggestedProgressForAssessment
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
            getSuggestedProgressForGrade,
            getSuggestedProgressForAssessment
        )

        val expectedTypeToPerformanceList = listOf(
            LabTopicType to SumProgress(4f, 4f)
        )

        assertEquals(
            expectedTypeToPerformanceList,
            useCase(studentId, subjectId, Labs.map { it.id })
        )
    }

    @Test
    fun shouldReturn_0_performance_whenAllAssessmentsFailedToPass() = runTest {
        val studentTotalPerformance = Assessments.map { lab ->
            lab to Performance(
                null, null,
                PerformanceItem.Assessment.FAIL, listOf(PerformanceItem.Attendance.Present)
            )
        }

        coEvery {
            getTotalStudentPerformance(studentId, subjectId)
        } returns flow { emit(studentTotalPerformance) }

        val useCase = GetSubjectStudentSummaryPerformanceUseCase(
            getTotalStudentPerformance,
            getSuggestedProgressForGrade,
            getSuggestedProgressForAssessment
        )

        val expectedTypeToPerformanceList = listOf(
            AssessmentTopicType to SumProgress(0f, 4f)
        )

        assertEquals(
            expectedTypeToPerformanceList,
            useCase(studentId, subjectId, Assessments.map { it.id })
        )
    }

    @Test
    fun shouldReturn_100_performance_whenAllAssessmentsPassed() = runTest {
        val studentTotalPerformance = Assessments.map { lab ->
            lab to Performance(
                null, null,
                PerformanceItem.Assessment.PASS, listOf(PerformanceItem.Attendance.Present)
            )
        }

        coEvery {
            getTotalStudentPerformance(studentId, subjectId)
        } returns flow { emit(studentTotalPerformance) }

        val useCase = GetSubjectStudentSummaryPerformanceUseCase(
            getTotalStudentPerformance,
            getSuggestedProgressForGrade,
            getSuggestedProgressForAssessment
        )

        val expectedTypeToPerformanceList = listOf(
            AssessmentTopicType to SumProgress(4f, 4f)
        )

        assertEquals(
            expectedTypeToPerformanceList,
            useCase(studentId, subjectId, Assessments.map { it.id })
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

        val AssessmentTopicType = TopicType(2, "Зачет", "", true, false, false, true, false, false)
        val Assessments = List(4) { i ->
            Topic(
                id = i,
                type = AssessmentTopicType,
                name = Topic.Name("Зачет", "", null, null, null)
            )
        }
    }
}