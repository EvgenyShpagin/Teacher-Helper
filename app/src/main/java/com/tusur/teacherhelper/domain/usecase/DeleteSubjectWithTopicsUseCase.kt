package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.SubjectRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import javax.inject.Inject

class DeleteSubjectWithTopicsUseCase @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val topicRepository: TopicRepository,
    private val deleteTopicUseCase: DeleteTopicUseCase,
) {
    suspend operator fun invoke(subjectId: Int) {
        val subjectTopicIds = topicRepository.getIdsBySubject(subjectId, withCancelled = true)
        subjectTopicIds.forEach { topicId ->
            deleteTopicUseCase(topicId, subjectId)
        }
        subjectRepository.delete(subjectId)
    }
}