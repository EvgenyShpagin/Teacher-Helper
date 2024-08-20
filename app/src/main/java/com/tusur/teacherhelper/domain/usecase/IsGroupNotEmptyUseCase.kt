package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.GroupRepository

class IsGroupNotEmptyUseCase(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(groupId: Int): Boolean {
        return groupRepository.hasStudents(groupId)
    }
}
