package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.PerformanceItem

class GetSuggestedProgressForGradeUseCase {
    operator fun invoke(grade: PerformanceItem.Grade?): PerformanceItem.Progress {
        return when (grade) {
            PerformanceItem.Grade(5) -> PerformanceItem.Progress(1.0f)
            PerformanceItem.Grade(4) -> PerformanceItem.Progress(0.8f)
            PerformanceItem.Grade(3) -> PerformanceItem.Progress(0.5f)
            else -> PerformanceItem.Progress(0f)
        }
    }
}