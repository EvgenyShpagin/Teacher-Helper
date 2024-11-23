package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.repository.SubjectRepository
import javax.inject.Inject

class GetSubjectByTopicIdUseCase @Inject constructor(
    private val subjectRepository: SubjectRepository
) {
    suspend operator fun invoke(topicId: Int): Subject {
        return subjectRepository.getOfTopic(topicId)
    }
}
