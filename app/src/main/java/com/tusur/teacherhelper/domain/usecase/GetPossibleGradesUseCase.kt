package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.PerformanceItem

class GetPossibleGradesUseCase {
    operator fun invoke(): List<PerformanceItem.Grade> {
        return listOf(
            PerformanceItem.Grade(5),
            PerformanceItem.Grade(4),
            PerformanceItem.Grade(3),
            PerformanceItem.Grade(2),
            PerformanceItem.Grade(0),
        )
    }
}