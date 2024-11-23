package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.util.withoutUnwantedSpaces
import javax.inject.Inject

class CheckTopicNameAddTextUseCase @Inject constructor(
    private val getAvailableTopicOrdinal: GetAvailableTopicOrdinalUseCase
) {
    /**
     * Check if additional text tries to replace ordinal.
     * It is possible if User want to do it with ordinal >= available
     */
    suspend operator fun invoke(
        subjectId: Int,
        topicId: Int?,
        topicTypeId: Int,
        name: Topic.Name
    ): TopicAddTextInfo {
        val ordinalAsAddText = name.addText?.withoutUnwantedSpaces()?.toIntOrNull()
            ?: return TopicAddTextInfo.Clear
        val availableOrdinal = getAvailableTopicOrdinal(subjectId, topicId, topicTypeId)
        return if (availableOrdinal <= ordinalAsAddText) {
            TopicAddTextInfo.ContainsCorrectOrdinal(ordinalAsAddText)
        } else {
            TopicAddTextInfo.ContainsExistingOrdinal(ordinalAsAddText)
        }
    }

    sealed class TopicAddTextInfo {
        data class ContainsCorrectOrdinal(val ordinal: Int) : TopicAddTextInfo()
        data class ContainsExistingOrdinal(val ordinal: Int) : TopicAddTextInfo()
        data object Clear : TopicAddTextInfo()
    }
}
