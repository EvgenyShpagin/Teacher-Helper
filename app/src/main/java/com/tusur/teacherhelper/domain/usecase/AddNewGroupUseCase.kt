package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.repository.GroupRepository
import com.tusur.teacherhelper.domain.util.NO_ID

class AddNewGroupUseCase(
    private val addGroupToSubject: AddGroupToSubjectUseCase,
    private val groupRepository: GroupRepository
) {
    /**
     * Add new group and attach to subject
     */
    suspend operator fun invoke(subjectId: Int, groupNumber: String): Int {
        return invoke(groupNumber).also { newGroupId ->
            addGroupToSubject(subjectId, newGroupId)
        }
    }

    /**
     * Just add new group
     */
    suspend operator fun invoke(groupNumber: String): Int {
        val group = Group(id = NO_ID, number = groupNumber)
        return groupRepository.add(group)
    }
}
