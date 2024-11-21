package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SetStudentPerformanceUseCaseTest {

    private val studentPerformanceRepository: StudentPerformanceRepository = mockk()
    private val getOrAddClassDateId: GetOrAddClassDateIdUseCase = mockk()
    private val useCase = SetStudentPerformanceUseCase(
        studentPerformanceRepository, getOrAddClassDateId
    )

    private val studentId = 1
    private val topicId = 2
    private val datetimeMillis = 1633036800000L
    private val classDateId = 100

    @Before
    fun setUp() {
        coEvery { getOrAddClassDateId(datetimeMillis) } returns classDateId
    }

    @Test
    fun shouldAddNewPerformance_whenGradeIsProvidedAndNoPreviousDataExists() = runTest {
        val grade = PerformanceItem.Grade(5)
        coEvery {
            studentPerformanceRepository.getSetPerformance(topicId, studentId, classDateId)
        } returns null
        coEvery { studentPerformanceRepository.add(any(), any(), any(), any()) } just runs

        useCase(studentId, topicId, grade, datetimeMillis)

        coVerify {
            studentPerformanceRepository.add(
                studentId,
                topicId,
                classDateId,
                Performance(grade = grade, progress = null, attendance = null, assessment = null)
            )
        }
    }

    @Test
    fun shouldUpdatePerformance_whenGradeIsProvidedAndPreviousDataExists() = runTest {
        val grade = PerformanceItem.Grade(4)
        val existingPerformance = Performance(
            grade = PerformanceItem.Grade(3),
            progress = PerformanceItem.Progress(0.5f),
            attendance = null,
            assessment = null
        )
        coEvery {
            studentPerformanceRepository.getSetPerformance(topicId, studentId, classDateId)
        } returns existingPerformance
        coEvery { studentPerformanceRepository.update(any(), any(), any(), any()) } just runs

        useCase(studentId, topicId, grade, datetimeMillis)

        coVerify {
            studentPerformanceRepository.update(
                studentId,
                topicId,
                classDateId,
                existingPerformance.copy(grade = grade)
            )
        }
    }

    @Test
    fun shouldAddNewPerformance_whenProgressIsProvidedAndNoPreviousDataExists() = runTest {
        val progress = PerformanceItem.Progress(0.75f)
        coEvery {
            studentPerformanceRepository.getSetPerformance(topicId, studentId, classDateId)
        } returns null
        coEvery { studentPerformanceRepository.add(any(), any(), any(), any()) } just runs

        useCase(studentId, topicId, progress, datetimeMillis)

        coVerify {
            studentPerformanceRepository.add(
                studentId, topicId, classDateId,
                Performance(grade = null, progress = progress, attendance = null, assessment = null)
            )
        }
    }

    @Test
    fun shouldAddNewPerformance_whenAttendanceIsProvidedAndNoPreviousDataExists() = runTest {
        val attendance = PerformanceItem.Attendance.Present
        coEvery {
            studentPerformanceRepository.getSetPerformance(topicId, studentId, classDateId)
        } returns null
        coEvery { studentPerformanceRepository.add(any(), any(), any(), any()) } just runs

        useCase(studentId, topicId, attendance, datetimeMillis)

        coVerify {
            studentPerformanceRepository.add(
                studentId, topicId, classDateId,
                Performance(
                    grade = null,
                    progress = null,
                    attendance = listOf(attendance),
                    assessment = null
                )
            )
        }
    }
}
