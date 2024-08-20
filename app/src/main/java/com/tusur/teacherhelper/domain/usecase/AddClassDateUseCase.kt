package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.repository.ClassDateRepository

class AddClassDateUseCase(private val classDateRepository: ClassDateRepository) {
    suspend operator fun invoke(datetimeMillis: Long): Int {
        return classDateRepository.add(datetimeMillis)
    }
}
