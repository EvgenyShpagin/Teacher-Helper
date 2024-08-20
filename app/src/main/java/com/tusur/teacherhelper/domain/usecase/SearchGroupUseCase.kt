package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.repository.GroupRepository

class SearchGroupUseCase(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(query: String): List<Group> {
        return groupRepository.search(query)
    }
}