package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.TopicType

class CanTopicTypeBeReplacedUseCase() {
    operator fun invoke(fromType: TopicType, toType: TopicType): Boolean {
        if (fromType == toType) return true
        val assessmentRefused = fromType.isAssessmentAcceptable && !toType.isAssessmentAcceptable
        val attendanceRefused = fromType.isAttendanceAcceptable && !toType.isAttendanceAcceptable
        val gradeRefused = fromType.isGradeAcceptable && !toType.isGradeAcceptable
        val progressRefused = fromType.isProgressAcceptable && !toType.isProgressAcceptable
        return !(assessmentRefused || attendanceRefused || gradeRefused || progressRefused)
    }
}