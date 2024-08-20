package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.ClassTime
import com.tusur.teacherhelper.domain.repository.ClassTimeRepository

class GetAllClassTimeUseCase(private val classTimeRepository: ClassTimeRepository) {
    operator fun invoke(): List<ClassTime> {
        return classTimeRepository.getAll()
    }
}