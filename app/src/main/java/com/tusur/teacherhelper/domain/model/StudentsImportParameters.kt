package com.tusur.teacherhelper.domain.model

data class StudentsImportParameters(
    val sheetIndex: Int,
    val columnIndex: Int,
    val firstRowIndex: Int,
    val hasMultipleSheets: Boolean,
    val namesAreSeparated: Boolean
)