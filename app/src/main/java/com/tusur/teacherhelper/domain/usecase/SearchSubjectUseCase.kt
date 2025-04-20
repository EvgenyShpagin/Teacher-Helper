package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.repository.SubjectRepository
import jakarta.inject.Inject

class SearchSubjectUseCase @Inject constructor(private val subjectRepository: SubjectRepository) {
    suspend operator fun invoke(query: String): List<Subject> {
        return subjectRepository.search(query)
    }
}