package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.usecase.CheckTopicNameAddTextUseCase.TopicAddTextInfo.Clear
import com.tusur.teacherhelper.domain.usecase.CheckTopicNameAddTextUseCase.TopicAddTextInfo.ContainsCorrectOrdinal
import com.tusur.teacherhelper.domain.usecase.CheckTopicNameAddTextUseCase.TopicAddTextInfo.ContainsExistingOrdinal
import com.tusur.teacherhelper.domain.util.NO_ID
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import java.lang.IllegalArgumentException

class CreateSubjectTopicUseCaseTest {

    private val topicRepository: TopicRepository = mockk()
    private val checkTopicNameAddText: CheckTopicNameAddTextUseCase = mockk()
    private val useCase = CreateSubjectTopicUseCase(topicRepository, checkTopicNameAddText)

    private val subjectId = 1
    private val topicType = TopicType(
        id = 2,
        name = "Lab",
        shortName = "",
        // does not matter
        false, false, false, false, false, false
    )
    private val topicName = Topic.Name("Lab", "", addText = "3", null, null)

    @Test
    fun shouldCreateTopicWithFixedName_whenAddTextIsCorrectOrdinal() = runTest {
        val ordinalInText = 3
        coEvery {
            checkTopicNameAddText(subjectId, null, topicType.id, topicName)
        } returns ContainsCorrectOrdinal(ordinalInText)

        val expectedFixedName = topicName.copy(addText = null, ordinal = ordinalInText)
        coEvery { topicRepository.create(subjectId, any()) } returns 10

        val result = useCase(subjectId, topicType, topicName)

        coVerify {
            topicRepository.create(
                subjectId = subjectId,
                topic = Topic(
                    id = NO_ID,
                    type = topicType,
                    name = expectedFixedName
                )
            )
        }
        assertEquals(10, result)
    }

    @Test
    fun shouldCreateTopicWithOriginalName_whenAddTextIsClear() = runTest {
        coEvery {
            checkTopicNameAddText(subjectId, null, topicType.id, topicName)
        } returns Clear

        coEvery { topicRepository.create(subjectId, any()) } returns 15

        val result = useCase(subjectId, topicType, topicName)

        coVerify {
            topicRepository.create(
                subjectId,
                Topic(
                    id = NO_ID,
                    type = topicType,
                    name = topicName
                )
            )
        }
        assertEquals(15, result)
    }

    @Test
    fun shouldThrowException_whenAddTextContainsExistingOrdinal() = runTest {
        val ordinalInText = 2
        coEvery {
            checkTopicNameAddText(subjectId, null, topicType.id, topicName)
        } returns ContainsExistingOrdinal(
            ordinalInText
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase(subjectId, topicType, topicName) }
        }
        assertEquals(
            "Cannot create topic with additional text in name that contains existing ordinal",
            exception.message
        )

        coVerify(exactly = 0) { topicRepository.create(any(), any()) }
    }
}
