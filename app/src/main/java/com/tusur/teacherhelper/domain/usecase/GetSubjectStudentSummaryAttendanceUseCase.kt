package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.SumProgress
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.repository.StudentPerformanceRepository
import com.tusur.teacherhelper.domain.repository.TopicRepository
import com.tusur.teacherhelper.domain.repository.TopicTypeRepository
import javax.inject.Inject

class GetSubjectStudentSummaryAttendanceUseCase @Inject constructor(
    private val studentPerformanceRepository: StudentPerformanceRepository,
    private val topicTypeRepository: TopicTypeRepository,
    private val topicRepository: TopicRepository,
) {
    suspend operator fun invoke(
        studentId: Int,
        subjectId: Int,
        takenInAccountTopicIds: List<Int>
    ): List<Pair<TopicType, SumProgress<Float>>> {
        val topicTypesWithAttendance = topicTypeRepository.getWithAttendance()
        val topicWithAttendanceIds = topicRepository.getOfTypes(
            topicTypesWithAttendance, subjectId
        ).map { it.id }
        val allTopicsAttendance = studentPerformanceRepository
            .getAttendance(topicWithAttendanceIds, studentId)
        val requiredTopicsAttendance =
            allTopicsAttendance.filter { it.first.id in takenInAccountTopicIds }

        val typesAttendance = ArrayList<Pair<TopicType, SumProgress<Float>>>(
            requiredTopicsAttendance.count()
        )

        requiredTopicsAttendance.forEach { (topic, attendance) ->
            val existingTypeProgress = typesAttendance.find { it.first == topic.type }
            if (existingTypeProgress != null) {
                val itsIndex = typesAttendance.indexOf(existingTypeProgress)
                val currentSumProgress = existingTypeProgress.second
                val newSumProgress = currentSumProgress.copy(
                    reached = currentSumProgress.reached + attendance.toProgressFraction(),
                    total = currentSumProgress.total + 1
                )
                typesAttendance[itsIndex] = existingTypeProgress.copy(second = newSumProgress)
            } else {
                typesAttendance.add(
                    topic.type to SumProgress(attendance.toProgressFraction(), 1f)
                )
            }

        }
        return typesAttendance
    }

    private fun PerformanceItem.Attendance.toProgressFraction(): Float {
        return when (this) {
            PerformanceItem.Attendance.Absent -> 0f
            else -> 1f
        }
    }
}