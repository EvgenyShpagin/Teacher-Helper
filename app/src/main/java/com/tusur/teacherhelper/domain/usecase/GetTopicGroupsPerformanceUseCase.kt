package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTopicGroupsPerformanceUseCase @Inject constructor(
    private val getTopicGroupPerformance: GetTopicGroupPerformanceUseCase,
    private val getGroupById: GetGroupByIdUseCase
) {
    suspend operator fun invoke(
        topicId: Int,
        groupIds: List<Int>,
        datetimeMillis: Long
    ): Flow<List<Pair<Group, List<Pair<Student, Performance>>>>> {
        return combine(
            flows = List(groupIds.count()) { i ->
                getTopicGroupPerformance(topicId = topicId, groupId = groupIds[i], datetimeMillis)
                    .map { list -> groupIds[i] to list }
            }
        ) { arrayOfStudentPerformance ->
            arrayOfStudentPerformance.map { (groupId, studentPerformanceList) ->
                getGroupById(groupId) to studentPerformanceList
            }
        }
    }
}