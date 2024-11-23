package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.repository.SubjectGroupRepository
import javax.inject.Inject

class SearchSubjectGroupUseCase @Inject constructor(
    private val subjectGroupRepository: SubjectGroupRepository
) {
    suspend operator fun invoke(subject: Subject, query: String): List<Group> {
        return subjectGroupRepository.searchGroup(subject, query)
    }
}
