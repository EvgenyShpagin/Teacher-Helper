package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.error.DeadlineUpdateError
import com.tusur.teacherhelper.domain.model.error.DeleteTopicError
import com.tusur.teacherhelper.domain.repository.ClassDateRepository
import com.tusur.teacherhelper.domain.repository.SubjectGroupRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.domain.util.Success
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteTopicUseCase @Inject constructor(
    private val topicRepository: TopicRepository,
    private val subjectGroupRepository: SubjectGroupRepository,
    private val classDateRepository: ClassDateRepository,
    private val setTopicDeadline: SetTopicDeadlineUseCase,
    private val deletePerformance: DeletePerformanceUseCase,
    private val getClassDatetime: GetClassDatetimeUseCase
) {
    suspend operator fun invoke(topicId: Int, subjectId: Int): Result<Unit, DeleteTopicError> {
        setTopicDeadline(topicId, null).onFailure { error ->
            if (error == DeadlineUpdateError.OtherTopicsDependOn) {
                return Result.Error(DeleteTopicError.OthersDependOnTopicDeadline)
            }
        }
        val classDays = getClassDatetime(topicId)
        val groups = subjectGroupRepository.getBySubject(subjectId).first()
        deletePerformance(topicId, groups.map { it.id }, classDays)
        topicRepository.delete(topicId)
        classDays.forEach { datetime ->
            classDateRepository.delete(datetime.toMillis())
        }
        return Result.Success()
    }
}
