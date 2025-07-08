package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.ClassTime
import com.tusur.teacherhelper.domain.model.Datetime
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Test

class GetSharedClassTimeUseCaseTest {

    val getSharedClassDatetime = mockk<GetSharedClassDatetimeUseCase>()
    val getAllClassTimeUseCase = mockk<GetAllClassTimeUseCase>()

    @Before
    fun setup() {
        coEvery { getAllClassTimeUseCase() } returns listOf(
            ClassTime(initTime = LocalTime(8, 50), finishTime = LocalTime(10, 25)),
            ClassTime(initTime = LocalTime(10, 40), finishTime = LocalTime(12, 15)),
            ClassTime(initTime = LocalTime(13, 15), finishTime = LocalTime(14, 50)),
        )
    }

    @Test
    fun shouldReturnCorrectClassTime_whenMultipleSharedDatetimeExist() = runTest {
        val topicId = 1
        val groupIds = listOf(1, 2)
        val classDate = LocalDate(2024, 10, 15)
        val classDatetimeList = listOf(
            Datetime(classDate, LocalTime(hour = 8, minute = 50)),
            Datetime(classDate, LocalTime(hour = 10, minute = 40)),
        )

        coEvery { getSharedClassDatetime(topicId, groupIds) } returns classDatetimeList

        val useCase = GetSharedClassTimeUseCase(getSharedClassDatetime, getAllClassTimeUseCase)

        assertEquals(
            listOf(
                ClassTime(initTime = LocalTime(8, 50), finishTime = LocalTime(10, 25)),
                ClassTime(initTime = LocalTime(10, 40), finishTime = LocalTime(12, 15))
            ),
            useCase(topicId, groupIds, classDate)
        )
    }

    @Test
    fun shouldReturnNoClassTime_whenNoSharedDatetimeExist() = runTest {
        val topicId = 1
        val groupIds = listOf(1, 2)
        val classDate = LocalDate(2024, 10, 15)

        coEvery { getSharedClassDatetime(topicId, groupIds) } returns emptyList()

        val useCase = GetSharedClassTimeUseCase(getSharedClassDatetime, getAllClassTimeUseCase)

        assertEquals(
            listOf<ClassTime>(),
            useCase(topicId, groupIds, classDate)
        )
    }

}