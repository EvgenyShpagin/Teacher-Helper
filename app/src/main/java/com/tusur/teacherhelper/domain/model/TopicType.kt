package com.tusur.teacherhelper.domain.model

data class TopicType(
    val id: Int,
    val name: String,
    val shortName: String,
    val canDeadlineBeSpecified: Boolean,
    val isGradeAcceptable: Boolean,
    val isProgressAcceptable: Boolean,
    val isAssessmentAcceptable: Boolean,
    val isAttendanceAcceptable: Boolean,
    val isAttendanceForOneClassOnly: Boolean
) {
    init {
        // Нельзя выставить оценку и зачет/незачет одновременно
        assert(!isAssessmentAcceptable || !isGradeAcceptable)
    }
}

const val LECTURE_ID = 1
const val LAB_ID = 2
const val PRACTICE_ID = 3

val PRIMARY_TOPIC_TYPES_IDS = intArrayOf(LECTURE_ID, LAB_ID, PRACTICE_ID)