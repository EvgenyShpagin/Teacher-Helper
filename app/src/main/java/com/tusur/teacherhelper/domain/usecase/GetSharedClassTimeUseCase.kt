package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.ClassTime
import com.tusur.teacherhelper.domain.model.Date
import javax.inject.Inject

class GetSharedClassTimeUseCase @Inject constructor(
    private val getSharedClassDatetime: GetSharedClassDatetimeUseCase,
    private val getAllClassTimeUseCase: GetAllClassTimeUseCase
) {
    suspend operator fun invoke(
        topicId: Int,
        groupsIds: List<Int>,
        classDate: Date
    ): List<ClassTime> {
        val initTimeList = getSharedClassDatetime(topicId, groupsIds)
            .filter { it.getDate() == classDate }
            .map { it.getTime() }
        val allClassTime = getAllClassTimeUseCase()
        val classTimeList = ArrayList<ClassTime>(groupsIds.count())
        initTimeList.forEach { time ->
            val correspondingClassTime = allClassTime.find { time in it } ?: return@forEach
            classTimeList.add(correspondingClassTime)
        }
        return classTimeList
    }
}