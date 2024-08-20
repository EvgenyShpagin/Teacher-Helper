package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.repository.SubjectGroupRepository

class SearchSubjectGroupUseCase(private val subjectGroupRepository: SubjectGroupRepository) {
    suspend operator fun invoke(subject: Subject, query: String): List<Group> {
        return subjectGroupRepository.searchGroup(subject, query)
    }
}
