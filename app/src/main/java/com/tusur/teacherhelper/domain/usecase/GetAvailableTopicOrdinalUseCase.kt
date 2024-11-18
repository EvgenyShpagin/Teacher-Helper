package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.TopicRepository

class GetAvailableTopicOrdinalUseCase(private val topicRepository: TopicRepository) {
    suspend operator fun invoke(subjectId: Int, topicId: Int?, topicTypeId: Int): Int {
        val savedTopic = topicId?.let { topicRepository.getById(it) }
        return if (savedTopic?.name?.ordinal != null) {
            savedTopic.name.ordinal
        } else {
            val allSameType = topicRepository.getAllSameType(subjectId, topicTypeId)
            if (allSameType.isEmpty()) return 1

            val maxSetOrdinal = allSameType.maxOf { it.name.ordinal ?: 0 }

            return maxSetOrdinal + 1
        }
    }
}