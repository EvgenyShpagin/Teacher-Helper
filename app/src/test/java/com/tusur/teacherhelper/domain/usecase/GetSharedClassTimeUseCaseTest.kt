package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.ClassTime
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.domain.model.Datetime
import com.tusur.teacherhelper.domain.model.Time
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetSharedClassTimeUseCaseTest {

    val getSharedClassDatetime = mockk<GetSharedClassDatetimeUseCase>()
    val getAllClassTimeUseCase = mockk<GetAllClassTimeUseCase>()

    @Before
    fun setup() {
        coEvery { getAllClassTimeUseCase() } returns listOf(
            ClassTime(initTime = Time(8, 50), finishTime = Time(10, 25)),
            ClassTime(initTime = Time(10, 40), finishTime = Time(12, 15)),
            ClassTime(initTime = Time(13, 15), finishTime = Time(14, 50)),
        )
    }

    @Test
    fun shouldReturnCorrectClassTime_whenMultipleSharedDatetimeExist() = runTest {
        val topicId = 1
        val groupIds = listOf(1, 2)
        val classDate = Date(2024, 10, 15)
        val classDatetimeList = listOf(
            Datetime(classDate, Time(hour = 8, minute = 50)),
            Datetime(classDate, Time(hour = 10, minute = 40)),
        )

        coEvery { getSharedClassDatetime(topicId, groupIds) } returns classDatetimeList

        val useCase = GetSharedClassTimeUseCase(getSharedClassDatetime, getAllClassTimeUseCase)

        assertEquals(
            listOf(
                ClassTime(initTime = Time(8, 50), finishTime = Time(10, 25)),
                ClassTime(initTime = Time(10, 40), finishTime = Time(12, 15))
            ),
            useCase(topicId, groupIds, classDate)
        )
    }

    @Test
    fun shouldReturnNoClassTime_whenNoSharedDatetimeExist() = runTest {
        val topicId = 1
        val groupIds = listOf(1, 2)
        val classDate = Date(2024, 10, 15)

        coEvery { getSharedClassDatetime(topicId, groupIds) } returns emptyList<Datetime>()

        val useCase = GetSharedClassTimeUseCase(getSharedClassDatetime, getAllClassTimeUseCase)

        assertEquals(
            listOf<ClassTime>(),
            useCase(topicId, groupIds, classDate)
        )
    }

}