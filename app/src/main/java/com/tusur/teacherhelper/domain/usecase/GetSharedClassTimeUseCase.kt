package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.ClassTime
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetSharedClassTimeUseCase @Inject constructor(
    private val getSharedClassDatetime: GetSharedClassDatetimeUseCase,
    private val getAllClassTimeUseCase: GetAllClassTimeUseCase
) {
    suspend operator fun invoke(
        topicId: Int,
        groupsIds: List<Int>,
        classDate: LocalDate
    ): List<ClassTime> {
        val initTimeList = getSharedClassDatetime(topicId, groupsIds)
            .filter { it.date == classDate }
            .map { it.time }
        val allClassTime = getAllClassTimeUseCase()
        val classTimeList = ArrayList<ClassTime>(groupsIds.count())
        initTimeList.forEach { time ->
            val correspondingClassTime = allClassTime.find { time in it } ?: return@forEach
            classTimeList.add(correspondingClassTime)
        }
        return classTimeList
    }
}