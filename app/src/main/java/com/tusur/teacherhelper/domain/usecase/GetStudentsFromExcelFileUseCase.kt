package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.StudentsImportParameters
import com.tusur.teacherhelper.domain.model.error.ExcelStudentImportError
import com.tusur.teacherhelper.domain.util.NO_ID
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import java.io.File


class GetStudentsFromExcelFileUseCase(private val getWorkbookFromFile: GetWorkbookFromFileUseCase) {

    private val tempNames = Array(3) { "" }

    operator fun invoke(
        excelFileParameters: StudentsImportParameters,
        file: File
    ): Result<List<Student>, ExcelStudentImportError> {
        val workbook = getWorkbookFromFile(file)!!
        val studentList = ArrayList<Student>()
        val sheet = workbook.getSheetAt(excelFileParameters.sheetIndex)

        for (rowIndex in excelFileParameters.firstRowIndex until sheet.lastRowNum.coerceAtLeast(1)) {
            val row = sheet.getRow(rowIndex)
            val student = if (excelFileParameters.namesAreSeparated) {
                getStudentFromMultipleCells(row, excelFileParameters.firstRowIndex)
            } else {
                getStudentFromCell(row.getCell(excelFileParameters.columnIndex))
            }
            student?.let { studentList.add(it) }
        }

        return if (studentList.isEmpty()) {
            Result.Error(ExcelStudentImportError.NO_FOUND)
        } else {
            Result.Success(studentList)
        }
    }

    private fun getStudentFromMultipleCells(row: Row, firstIndex: Int): Student? {
        emptyTempNames()
        val lastIndex = (firstIndex + 3).coerceAtMost(row.lastCellNum.toInt())
        var nameIndex = 0
        var atLeastOneNameIsNotEmpty = false
        for (cellIndex in firstIndex until lastIndex) {
            tempNames[nameIndex++] = row.getCell(cellIndex).stringCellValue.trim()
                .also {
                    if (it.isNotBlank()) {
                        atLeastOneNameIsNotEmpty = true
                    }
                }
        }
        return if (atLeastOneNameIsNotEmpty) {
            Student(NO_ID, Student.Name(tempNames[0], tempNames[1], tempNames[2]))
        } else {
            null
        }
    }

    private fun getStudentFromCell(cell: Cell): Student? {
        val names = cell.stringCellValue.split(' ')
        val lastName = names.getOrNull(0)
        val firstName = names.getOrNull(1)
        val middleName = names.getOrNull(2)
        return if (lastName != null && firstName != null) {
            Student(NO_ID, Student.Name(lastName, firstName, middleName ?: ""))
        } else {
            null
        }
    }

    private fun emptyTempNames() {
        for (i in 0 until 3) {
            tempNames[i] = ""
        }
    }
}