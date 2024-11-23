package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.SubjectGroupRepository
import javax.inject.Inject

class DeleteSubjectGroupUseCase @Inject constructor(
    private val subjectGroupRepository: SubjectGroupRepository
) {
    suspend operator fun invoke(subjectId: Int, groupId: Int) {
        subjectGroupRepository.remove(subjectId, groupId)
    }
}
