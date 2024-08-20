package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.repository.GroupRepository

class GetGroupByIdUseCase(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(id: Int): Group {
        return groupRepository.getById(id)
    }
}