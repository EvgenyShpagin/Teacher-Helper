package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.repository.GroupRepository
import com.tusur.teacherhelper.domain.repository.SubjectGroupRepository
import com.tusur.teacherhelper.domain.util.GLOBAL_TOPICS_SUBJECT_ID
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSubjectGroupsUseCase @Inject constructor(
    private val subjectGroupRepository: SubjectGroupRepository,
    private val groupRepository: GroupRepository
) {
    operator fun invoke(subjectId: Int): Flow<List<Group>> {
        return if (subjectId == GLOBAL_TOPICS_SUBJECT_ID) {
            groupRepository.getAll()
        } else {
            subjectGroupRepository.getBySubject(subjectId)
        }
    }
}
