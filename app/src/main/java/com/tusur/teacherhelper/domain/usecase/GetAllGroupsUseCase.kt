package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow

class GetAllGroupsUseCase(private val groupRepository: GroupRepository) {
    operator fun invoke(): Flow<List<Group>> {
        return groupRepository.getAll()
    }
}