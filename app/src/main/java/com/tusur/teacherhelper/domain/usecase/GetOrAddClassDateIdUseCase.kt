package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.ClassDateRepository
import javax.inject.Inject

class GetOrAddClassDateIdUseCase @Inject constructor(
    private val classDateRepository: ClassDateRepository
) {
    suspend operator fun invoke(datetimeMillis: Long): Int {
        return classDateRepository.getIdByMillis(datetimeMillis)
            ?: classDateRepository.add(datetimeMillis)
    }
}