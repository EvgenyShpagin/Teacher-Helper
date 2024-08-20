package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.util.NO_ID

class CreateSubjectTopicUseCase(
    private val topicRepository: TopicRepository,
    private val checkTopicNameAddText: CheckTopicNameAddTextUseCase
) {
    suspend operator fun invoke(
        subjectId: Int,
        topicType: TopicType,
        topicName: Topic.Name
    ): Int {
        val fixedName = checkTopicNameAddText(
            subjectId = subjectId,
            topicId = null,
            topicTypeId = topicType.id,
            name = topicName
        ).let { checkResult ->
            when (checkResult) {
                CheckTopicNameAddTextUseCase.TopicAddTextInfo.Clear -> topicName
                is CheckTopicNameAddTextUseCase.TopicAddTextInfo.ContainsCorrectOrdinal ->
                    topicName.copy(addText = null, ordinal = topicName.addText!!.trim().toInt())

                else -> throw IllegalArgumentException(
                    "Cannot create topic with additional text in name that contains existing ordinal"
                )
            }
        }
        val topic = Topic(
            id = NO_ID,
            type = topicType,
            name = fixedName
        )
        return topicRepository.create(subjectId, topic)
    }
}