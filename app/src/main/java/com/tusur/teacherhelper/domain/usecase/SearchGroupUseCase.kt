package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.repository.GroupRepository
import javax.inject.Inject

class SearchGroupUseCase @Inject constructor(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(query: String): List<Group> {
        return groupRepository.search(query)
    }
}