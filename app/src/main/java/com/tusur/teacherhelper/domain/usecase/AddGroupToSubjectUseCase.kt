package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.SubjectGroupRepository

class AddGroupToSubjectUseCase(private val subjectGroupRepository: SubjectGroupRepository) {
    suspend operator fun invoke(subjectId: Int, groupId: Int) {
        subjectGroupRepository.add(subjectId, groupId)
    }
}
