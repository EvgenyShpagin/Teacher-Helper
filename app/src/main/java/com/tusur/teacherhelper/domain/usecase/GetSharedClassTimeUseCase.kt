package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.ClassTime

class GetSharedClassTimeUseCase(
    private val getSharedClassDatetime: GetSharedClassDatetimeUseCase,
    private val getAllClassTimeUseCase: GetAllClassTimeUseCase
) {
    suspend operator fun invoke(topicId: Int, groupsIds: List<Int>): List<ClassTime> {
        val initTimeList = getSharedClassDatetime(topicId, groupsIds)
            .map { it.getTime() }.distinct()
        val allClassTime = getAllClassTimeUseCase()
        val classTimeList = ArrayList<ClassTime>(groupsIds.count())
        initTimeList.forEach { time ->
            val correspondingClassTime = allClassTime.find { time in it } ?: return@forEach
            classTimeList.add(correspondingClassTime)
        }
        return classTimeList
    }
}