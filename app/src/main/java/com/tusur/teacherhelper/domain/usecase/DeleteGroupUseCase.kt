package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.GroupRepository

class DeleteGroupUseCase(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(groupId: Int) {
        groupRepository.deleteWithStudents(groupId)
    }
}