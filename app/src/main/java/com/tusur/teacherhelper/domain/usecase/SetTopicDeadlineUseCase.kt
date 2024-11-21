package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Deadline
import com.tusur.teacherhelper.domain.model.error.DeadlineUpdateError
import com.tusur.teacherhelper.domain.model.isDeadlineOwner
import com.tusur.teacherhelper.domain.repository.DeadlineRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.domain.util.Success

class SetTopicDeadlineUseCase(
    private val topicRepository: TopicRepository,
    private val deadlineRepository: DeadlineRepository
) {

    suspend operator fun invoke(topicId: Int, deadline: Deadline?) {
        val currentDeadline = topicRepository.getById(topicId)!!.deadline
        when {
            currentDeadline == null && deadline != null -> setTopicDeadline(topicId, deadline)
            currentDeadline != null && deadline == null -> removeTopicDeadline(topicId)
            currentDeadline != null && deadline != null -> replaceTopicDeadline(topicId, deadline)
        }
    }

    private suspend fun replaceTopicDeadline(topicId: Int, newDeadline: Deadline) {
        removeTopicDeadline(topicId)
            .onSuccess { data ->
                topicRepository.setDeadline(topicId, insertDeadlineIfNeeded(newDeadline))
            }.onFailure { error ->
                if (error == DeadlineUpdateError.OtherTopicsDependOn) {
                    // TODO: resolve problem
                }
            }
    }

    private suspend fun removeTopicDeadline(topicId: Int): Result<Unit, DeadlineUpdateError> {
        val topic = topicRepository.getById(topicId)!!
        if (topic.isDeadlineOwner) {
            val sameDeadlineTopicCount =
                topicRepository.countSameDeadlineTopics(topic.deadline!!.id)
            // If only this topic is using its deadline
            if (sameDeadlineTopicCount == 1) {
                topicRepository.setDeadline(topicId, null)
                deadlineRepository.delete(topic.deadline)
            } else {
                // TODO: resolve problem
                return Result.Error(DeadlineUpdateError.OtherTopicsDependOn)
            }
        } else {
            topicRepository.setDeadline(topicId, null)
        }
        return Result.Success()
    }

    private suspend fun setTopicDeadline(topicId: Int, deadline: Deadline) {
        topicRepository.setDeadline(topicId, insertDeadlineIfNeeded(deadline))
    }

    private suspend fun insertDeadlineIfNeeded(deadline: Deadline): Deadline {
        return if (deadline.id == NO_ID) {
            val insertedId = deadlineRepository.insert(deadline)
            deadline.copy(id = insertedId)
        } else {
            deadline
        }
    }
}