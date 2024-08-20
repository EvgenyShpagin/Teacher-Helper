package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.repository.SubjectRepository
import com.tusur.teacherhelper.domain.util.GLOBAL_TOPICS_SUBJECT_ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSubjectListUseCase(private val subjectRepository: SubjectRepository) {
    operator fun invoke(): Flow<List<Subject>> {
        return subjectRepository.getAll().map { list ->
            list.filterNot { it.id == GLOBAL_TOPICS_SUBJECT_ID }
        }
    }
}