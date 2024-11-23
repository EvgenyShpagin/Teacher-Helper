package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.GroupRepository
import javax.inject.Inject

class DoesGroupNumberAlreadyExistUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupNumber: String): Boolean {
        return groupRepository.exists(groupNumber)
    }
}