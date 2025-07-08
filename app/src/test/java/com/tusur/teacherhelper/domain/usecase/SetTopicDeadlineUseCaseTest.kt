package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Deadline
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.error.DeadlineUpdateError
import com.tusur.teacherhelper.domain.repository.DeadlineRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.domain.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SetTopicDeadlineUseCaseTest {

    val topicId = 1
    val otherTopicId = topicId + 1

    val topicRepository = mockk<TopicRepository>()
    val deadlineRepository = mockk<DeadlineRepository>()

    val currentTopic = mockk<Topic>()

    val useCase = SetTopicDeadlineUseCase(topicRepository, deadlineRepository)

    @Before
    fun setup() {
        coEvery { topicRepository.getById(topicId) } returns currentTopic
        coEvery { topicRepository.setDeadline(topicId, any()) } just runs

        every { currentTopic.id } returns topicId
    }

    @Test
    fun shouldReturnSuccess_whenJustAddExistingDeadline() = runTest {
        val currentDeadline: Deadline? = null
        val newDeadline = Deadline(1, LocalDate(2024, 10, 15), topicId) // already saved

        every { currentTopic.deadline } returns currentDeadline

        assertEquals(
            Result.Success<Unit, DeadlineUpdateError>(Unit),
            useCase(topicId, newDeadline)
        )
        // Additionally verify that set was called
        coVerify(exactly = 1) { topicRepository.setDeadline(topicId, newDeadline) }
    }

    @Test
    fun shouldInsertDeadline_whenItIsNotSavedYet() = runTest {
        val currentDeadline: Deadline? = null
        val newDeadline = Deadline(NO_ID, LocalDate(2024, 10, 15), topicId) // not yet saved

        every { currentTopic.deadline } returns currentDeadline
        coEvery { deadlineRepository.insert(newDeadline) } returns 0

        val result = useCase(topicId, newDeadline)

        coVerify(exactly = 1) { deadlineRepository.insert(newDeadline) }

        // Additionally assert that return value is Success
        assertEquals(
            Result.Success<Unit, DeadlineUpdateError>(Unit),
            result
        )
    }

    @Test
    fun shouldReturnSuccess_whenReplaceOtherTopicDeadlineWithSomeOther() = runTest {
        val currentDeadline = Deadline(1, LocalDate(2024, 10, 15), otherTopicId)
        val otherDeadline = Deadline(2, LocalDate(2024, 10, 16), otherTopicId)

        every { currentTopic.deadline } returns currentDeadline

        assertEquals(
            Result.Success<Unit, DeadlineUpdateError>(Unit),
            useCase(topicId, otherDeadline)
        )
        // Additionally verify that set was called
        coVerify(exactly = 1) { topicRepository.setDeadline(topicId, otherDeadline) }
    }

    @Test
    fun shouldReturnError_whenReplacingDeadlineReferencedByOtherTopics() = runTest {
        val currentDeadline = Deadline(1, LocalDate(2024, 10, 15), topicId)
        val otherDeadline = Deadline(2, LocalDate(2024, 10, 16), otherTopicId)

        every { currentTopic.deadline } returns currentDeadline
        coEvery { topicRepository.countSameDeadlineTopics(currentDeadline.id) } returns 2

        assertEquals(
            Result.Error<Unit, DeadlineUpdateError>(DeadlineUpdateError.OtherTopicsDependOn),
            useCase(topicId, otherDeadline)
        )
    }

    @Test
    fun shouldReturnSuccessAndDeleteDeadline_whenRemovingDeadlineNotReferencedByOtherTopics() =
        runTest {
            val currentDeadline = Deadline(1, LocalDate(2024, 10, 15), topicId)
            val otherDeadline = Deadline(2, LocalDate(2024, 10, 16), otherTopicId)

            every { currentTopic.deadline } returns currentDeadline
            coEvery { topicRepository.countSameDeadlineTopics(currentDeadline.id) } returns 1
            coEvery { deadlineRepository.delete(currentDeadline) } just runs

            assertEquals(
                Result.Success<Unit, DeadlineUpdateError>(Unit),
                useCase(topicId, otherDeadline)
            )

            coVerify(exactly = 1) { deadlineRepository.delete(currentDeadline) }
        }

    @Test
    fun shouldReturnError_whenRemovingDeadlineReferencedByOtherTopics() = runTest {
        val currentDeadline = Deadline(1, LocalDate(2024, 10, 15), topicId)
        val otherDeadline: Deadline? = null

        every { currentTopic.deadline } returns currentDeadline
        coEvery { topicRepository.countSameDeadlineTopics(currentDeadline.id) } returns 2

        assertEquals(
            Result.Error<Unit, DeadlineUpdateError>(DeadlineUpdateError.OtherTopicsDependOn),
            useCase(topicId, otherDeadline)
        )
    }
}