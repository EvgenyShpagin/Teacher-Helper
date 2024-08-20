package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.GroupRepository

class IsGroupAssociatedToAnySubjectUseCase(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(groupId: Int): Boolean {
        return groupRepository.isAssociatedToAnySubject(groupId)
    }
}