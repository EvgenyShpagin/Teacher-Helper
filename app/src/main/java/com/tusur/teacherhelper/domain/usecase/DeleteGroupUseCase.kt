package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.GroupRepository
import javax.inject.Inject

class DeleteGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: Int) {
        groupRepository.deleteWithStudents(groupId)
    }
}