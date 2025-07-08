package com.tusur.teacherhelper.domain.usecase

import android.util.Log
import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import com.tusur.teacherhelper.domain.repository.StudentRepository
import com.tusur.teacherhelper.domain.util.fromEpochMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class GetTopicGroupPerformanceUseCase @Inject constructor(
    private val studentRepository: StudentRepository,
    private val studentPerformanceRepository: StudentPerformanceRepository,
    private val getClassDateId: GetClassDateIdUseCase,
    private val addClassDate: AddClassDateUseCase
) {
    suspend operator fun invoke(
        topicId: Int,
        groupId: Int,
        datetimeMillis: Long
    ): Flow<List<Pair<Student, Performance>>> {
        val groupStudents = studentRepository.getAll(groupId)
        val classDateId = getClassDateId(datetimeMillis).also {
            if (it == null) {
                Log.d(
                    "TAG_1",
                    "GetTopicGroupPerformanceUseCase.invoke(): class date (${
                        LocalDateTime.fromEpochMillis(datetimeMillis)
                    }) does not exist in database"
                )
            }
        } ?: addClassDate(datetimeMillis)
        val studentsPerformance = studentPerformanceRepository
            .getSetPerformance(topicId, groupStudents, classDateId)

        return studentsPerformance.map { listStudentToPerformance ->
            groupStudents.map { groupStudent ->
                val studentWithPerformance = listStudentToPerformance
                    .find { (student, _) -> groupStudent.id == student.id }
                groupStudent to (studentWithPerformance?.second ?: emptyPerformance)
            }
        }
    }

    private companion object {
        val emptyPerformance = Performance(null, null, null, null)
    }
}