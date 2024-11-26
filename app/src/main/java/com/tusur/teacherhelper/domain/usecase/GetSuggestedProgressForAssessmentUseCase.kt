package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.PerformanceItem
import javax.inject.Inject

class GetSuggestedProgressForAssessmentUseCase @Inject constructor() {
    operator fun invoke(assessment: PerformanceItem.Assessment): PerformanceItem.Progress {
        return when (assessment) {
            PerformanceItem.Assessment.FAIL -> PerformanceItem.Progress(0f)
            PerformanceItem.Assessment.PASS -> PerformanceItem.Progress(1f)
        }
    }
}