package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.ClassDateRepository

class GetClassDateIdUseCase(private val classDateRepository: ClassDateRepository) {
    suspend operator fun invoke(datetimeMillis: Long): Int? {
        return classDateRepository.getIdByMillis(datetimeMillis)
    }
}