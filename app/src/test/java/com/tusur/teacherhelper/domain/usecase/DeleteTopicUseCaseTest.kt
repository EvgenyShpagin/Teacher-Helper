package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Datetime
import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.model.error.DeadlineUpdateError
import com.tusur.teacherhelper.domain.model.error.DeleteTopicError
import com.tusur.teacherhelper.domain.repository.SubjectGroupRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.domain.util.Success
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DeleteTopicUseCaseTest {

    private val topicId = 1
    private val subjectId = 2

    private val topicRepository = mockk<TopicRepository>()
    private val subjectGroupRepository = mockk<SubjectGroupRepository>()
    private val setTopicDeadline = mockk<SetTopicDeadlineUseCase>()
    private val deletePerformance = mockk<DeletePerformanceUseCase>()
    private val getClassDatetime = mockk<GetClassDatetimeUseCase>()

    private val useCase = DeleteTopicUseCase(
        topicRepository = topicRepository,
        subjectGroupRepository = subjectGroupRepository,
        setTopicDeadline = setTopicDeadline,
        deletePerformance = deletePerformance,
        getClassDatetime = getClassDatetime
    )

    @Before
    fun setup() {
        coEvery { topicRepository.delete(any()) } just runs
    }

    @Test
    fun shouldReturnSuccess_whenTopicIsDeleted() = runTest {
        val groups = listOf(Group(1, "430-3"), Group(2, "430-4"))
        val classDays = listOf(
            Datetime(2024, 10, 15, 8, 50),
            Datetime(2024, 10, 15, 10, 40)
        )

        coEvery { setTopicDeadline(topicId, null) } returns Result.Success()
        coEvery { getClassDatetime(topicId) } returns classDays
        coEvery { subjectGroupRepository.getBySubject(subjectId) } returns flowOf(groups)
        coEvery { deletePerformance(topicId, groups.map { it.id }, classDays) } just runs

        val result = useCase(topicId, subjectId)

        coVerify(exactly = 1) { setTopicDeadline(topicId, null) }
        coVerify(exactly = 1) { getClassDatetime(topicId) }
        coVerify(exactly = 1) { subjectGroupRepository.getBySubject(subjectId) }
        coVerify(exactly = 1) { deletePerformance(topicId, groups.map { it.id }, classDays) }
        coVerify(exactly = 1) { topicRepository.delete(topicId) }

        assertEquals(Result.Success<Unit, DeleteTopicError>(Unit), result)
    }

    @Test
    fun shouldReturnError_whenOtherTopicsDependOnTopicDeadline() = runTest {
        coEvery {
            setTopicDeadline(topicId, null)
        } returns Result.Error(DeadlineUpdateError.OtherTopicsDependOn)

        val result = useCase(topicId, subjectId)

        coVerify(exactly = 1) { setTopicDeadline(topicId, null) }
        coVerify(exactly = 0) { getClassDatetime(any()) }
        coVerify(exactly = 0) { subjectGroupRepository.getBySubject(any()) }
        coVerify(exactly = 0) { deletePerformance(topicId, any(), any<List<Datetime>>()) }
        coVerify(exactly = 0) { topicRepository.delete(any()) }

        assertEquals(
            Result.Error<Unit, DeleteTopicError>(DeleteTopicError.OthersDependOnTopicDeadline),
            result
        )
    }

    @Test
    fun shouldCallDeletePerformanceWithCorrectParameters_whenDeleteIsPossible() = runTest {
        val groups = listOf(Group(1, "430-3"), Group(2, "430-4"))
        val classDays = listOf(
            Datetime(2024, 10, 15, 8, 50),
            Datetime(2024, 10, 15, 10, 40)
        )

        coEvery { setTopicDeadline(topicId, null) } returns Result.Success()
        coEvery { getClassDatetime(topicId) } returns classDays
        coEvery { subjectGroupRepository.getBySubject(subjectId) } returns flowOf(groups)
        coEvery { deletePerformance(topicId, groups.map { it.id }, classDays) } just runs

        useCase(topicId, subjectId)

        coVerify(exactly = 1) { deletePerformance(topicId, groups.map { it.id }, classDays) }
    }
}
