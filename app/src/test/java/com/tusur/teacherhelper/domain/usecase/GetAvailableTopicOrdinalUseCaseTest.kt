package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.Topic.Name
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetAvailableTopicOrdinalUseCaseTest {

    val repository = mockk<TopicRepository>()
    var lastUsedId = 0

    @Before
    fun setup() {
        lastUsedId = 0
    }

    @Test
    fun shouldReturn_1_whenNoTopicsOfSameTypeExist() = runTest {
        coEvery { repository.getAllSameType(subjectId = 1, topicTypeId = 3) } returns emptyList()

        val useCase = GetAvailableTopicOrdinalUseCase(repository)

        assertEquals(1, useCase(subjectId = 1, topicId = null, topicTypeId = 3))
    }

    @Test
    fun shouldReturn_1_whenNoSameTypeTopicsWithOrdinal() = runTest {
        val sameTypeList = List(3) { i -> createPracticeTopic(ordinal = null) }

        val topic = sameTypeList.first()

        coEvery { repository.getById(topic.id) } returns topic
        coEvery { repository.getAllSameType(subjectId = 1, topicTypeId = 3) } returns sameTypeList

        val useCase = GetAvailableTopicOrdinalUseCase(repository)

        assertEquals(1, useCase(subjectId = 1, topicId = topic.id, topicTypeId = 3))
    }

    @Test
    fun shouldReturn_3_whenSameTypeTopicExistsWithOrdinal_2() = runTest {
        val sameTypeList = List(2) { i -> createPracticeTopic(i + 1) }

        coEvery { repository.getAllSameType(subjectId = 1, topicTypeId = 3) } returns sameTypeList

        val useCase = GetAvailableTopicOrdinalUseCase(repository)

        assertEquals(3, useCase(subjectId = 1, topicId = null, topicTypeId = 3))
    }

    @Test
    fun shouldReturnSavedOrdinal_whenSomeOrdinalSaved() = runTest {
        val sameTypeList = List(5) { i -> createPracticeTopic(ordinal = i + 1) }
        val topic = sameTypeList.first()

        coEvery { repository.getById(topic.id) } returns topic
        coEvery { repository.getAllSameType(subjectId = 1, topicTypeId = 3) } returns sameTypeList

        val useCase = GetAvailableTopicOrdinalUseCase(repository)

        assertEquals(1, useCase(subjectId = 1, topicId = topic.id, topicTypeId = 3))
    }

    private fun createPracticeTopic(ordinal: Int?): Topic {
        return Topic(
            id = ++lastUsedId,
            name = Name("Практика", "Практ", null, ordinal, null),
            type = TopicType(3, "Практика", "Практ", true, true, false, false, false, true),
            deadline = null
        )
    }
}