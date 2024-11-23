package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.util.withoutUnwantedSpaces
import javax.inject.Inject

class FormatStudentNameUseCase @Inject constructor() {
    operator fun invoke(name: String): Student.Name {
        val spaceFixedText = name.withoutUnwantedSpaces()
        return spaceFixedText.split(' ').map { word ->
            word.lowercase().replaceFirstChar { it.titlecaseChar() }
        }.let {
            Student.Name(
                lastName = it.getOrElse(0) { "" },
                firstName = it.getOrElse(1) { "" },
                middleName = it.getOrElse(2) { "" }
            )
        }
    }
}