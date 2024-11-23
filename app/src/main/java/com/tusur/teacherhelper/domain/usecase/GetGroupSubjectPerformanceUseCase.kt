package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.StudentSubjectPerformance
import com.tusur.teacherhelper.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject


class GetGroupSubjectPerformanceUseCase @Inject constructor(
    private val getSubjectStudentPerformance: GetSubjectStudentPerformanceUseCase,
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(
        subjectId: Int,
        groupId: Int
    ): Flow<List<StudentSubjectPerformance>> {
        val students = studentRepository.getAll(groupId)

        val eachStudentPerformance = List(students.count()) { i ->
            getSubjectStudentPerformance(students[i].id, subjectId)
        }

        return combine(eachStudentPerformance) { performanceList ->
            val studentsIterator = students.iterator()
            performanceList.map {
                StudentSubjectPerformance(
                    student = studentsIterator.next(),
                    topicsWithPerformance = it
                )
            }
        }
    }
}