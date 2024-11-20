package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CheckTopicNameAddTextUseCaseTest {

    private val getAvailableTopicOrdinal: GetAvailableTopicOrdinalUseCase = mockk()
    private val useCase = CheckTopicNameAddTextUseCase(getAvailableTopicOrdinal)

    private val subjectId = 1
    private val topicId = 2
    private val topicTypeId = 3

    @Test
    fun shouldReturnClear_whenAddTextIsNull() = runTest {
        val topicName = Topic.Name("TopicName", "", null, null, null)
        assertEquals(
            CheckTopicNameAddTextUseCase.TopicAddTextInfo.Clear,
            useCase(subjectId, topicId, topicTypeId, topicName)
        )
    }

    @Test
    fun shouldReturnClear_whenAddTextIsNotANumber() = runTest {
        val topicName = Topic.Name("TopicName", "", null, null, null)
        val result = useCase(subjectId, topicId, topicTypeId, topicName)
        assertEquals(CheckTopicNameAddTextUseCase.TopicAddTextInfo.Clear, result)
    }

    @Test
    fun shouldReturnContainsCorrectOrdinal_whenOrdinalIsGreaterThanOrEqualToAvailable() = runTest {
        val ordinalInText = 5
        val availableOrdinal = 4
        coEvery {
            getAvailableTopicOrdinal(
                subjectId,
                topicId,
                topicTypeId
            )
        } returns availableOrdinal

        val topicName = Topic.Name("TopicName", "", addText = ordinalInText.toString(), null, null)

        val result = useCase(subjectId, topicId, topicTypeId, topicName)

        assertEquals(
            CheckTopicNameAddTextUseCase.TopicAddTextInfo.ContainsCorrectOrdinal(ordinalInText),
            result
        )
    }

    @Test
    fun shouldReturnContainsExistingOrdinal_whenOrdinalIsLessThanAvailable() = runTest {
        val ordinalInText = 3
        val availableOrdinal = 4
        coEvery {
            getAvailableTopicOrdinal(
                subjectId,
                topicId,
                topicTypeId
            )
        } returns availableOrdinal

        val topicName = Topic.Name("TopicName", "", addText = ordinalInText.toString(), null, null)

        val result = useCase(subjectId, topicId, topicTypeId, topicName)

        assertEquals(
            CheckTopicNameAddTextUseCase.TopicAddTextInfo.ContainsExistingOrdinal(ordinalInText),
            result
        )
    }
}
