package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.repository.SubjectRepository

class GetSubjectByIdUseCase(private val subjectRepository: SubjectRepository) {
    suspend operator fun invoke(subjectId: Int): Subject {
        return subjectRepository.getById(subjectId)
    }
}
