package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupStudentsUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    operator fun invoke(groupId: Int): Flow<List<Student>> {
        return studentRepository.getAllAsFlow(groupId)
    }
}
