package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.error.TopicNameError
import com.tusur.teacherhelper.domain.usecase.CheckTopicNameAddTextUseCase.TopicAddTextInfo.Clear
import com.tusur.teacherhelper.domain.usecase.CheckTopicNameAddTextUseCase.TopicAddTextInfo.ContainsCorrectOrdinal
import com.tusur.teacherhelper.domain.usecase.CheckTopicNameAddTextUseCase.TopicAddTextInfo.ContainsExistingOrdinal
import com.tusur.teacherhelper.domain.util.Result
import javax.inject.Inject

class ValidateTopicNameUseCase @Inject constructor(
    private val getTopicByName: GetTopicByNameUseCase,
    private val checkTopicNameAddText: CheckTopicNameAddTextUseCase
) {
    suspend operator fun invoke(
        subjectId: Int,
        topicId: Int?,
        topicTypeId: Int,
        name: Topic.Name
    ): Result<Unit, TopicNameError> {
        val topicWithThisName = getTopicByName(name)
        return when {
            topicWithThisName == null -> {
                when (checkTopicNameAddText(subjectId, topicId, topicTypeId, name)) {
                    Clear -> Result.Success(Unit)
                    is ContainsCorrectOrdinal -> Result.Success(Unit)
                    is ContainsExistingOrdinal -> Result.Error(TopicNameError.ALREADY_EXISTS)
                }
            }

            else -> Result.Error(
                error = if (topicWithThisName.id == topicId) {
                    TopicNameError.NOT_CHANGED
                } else {
                    TopicNameError.ALREADY_EXISTS
                }
            )
        }
    }
}
