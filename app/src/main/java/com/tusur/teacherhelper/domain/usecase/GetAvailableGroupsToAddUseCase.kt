package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.repository.GroupRepository
import com.tusur.teacherhelper.domain.repository.SubjectGroupRepository
import kotlinx.coroutines.flow.first

class GetAvailableGroupsToAddUseCase(
    private val groupRepository: GroupRepository,
    private val subjectGroupRepository: SubjectGroupRepository
) {
    suspend operator fun invoke(subjectId: Int): List<Group> {
        val subjectGroups = subjectGroupRepository.getBySubject(subjectId).first()
        val allGroups = groupRepository.getAll().first()
        return allGroups.filter { it !in subjectGroups }
    }
}
