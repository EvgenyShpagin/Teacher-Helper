package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.repository.SubjectRepository

class AddSubjectUseCase(private val repository: SubjectRepository) {
    suspend operator fun invoke(subject: Subject) {
        repository.create(subject)
    }
}
