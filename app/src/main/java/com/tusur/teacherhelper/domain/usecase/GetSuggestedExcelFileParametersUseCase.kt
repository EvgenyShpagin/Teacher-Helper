package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.StudentsImportParameters
import com.tusur.teacherhelper.domain.model.error.ExcelStudentImportError
import com.tusur.teacherhelper.domain.util.Result
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import javax.inject.Inject


class GetSuggestedExcelFileParametersUseCase @Inject constructor(
    private val getWorkbookFromFile: GetWorkbookFromFileUseCase
) {
    operator fun invoke(
        file: File?
    ): Result<StudentsImportParameters, ExcelStudentImportError> {
        if (file == null) {
            return Result.Error(ExcelStudentImportError.FILE_NOT_CHOSEN)
        }
        val workbook = getWorkbookFromFile(file)
            ?: return Result.Error(ExcelStudentImportError.WRONG_FILE_FORMAT)

        val hasMultipleSheets = workbook.numberOfSheets > 1
        var studentsImportParameters: StudentsImportParameters? = null

        traverseValidSheetCells(workbook) { sheetIndex, cell ->
            if (cell.stringCellValue.matches(studentFullNameRegex)) {
                studentsImportParameters = StudentsImportParameters(
                    sheetIndex = sheetIndex,
                    columnIndex = cell.columnIndex,
                    firstRowIndex = cell.rowIndex,
                    hasMultipleSheets = hasMultipleSheets,
                    namesAreSeparated = false
                )
                return@traverseValidSheetCells true
            }
            return@traverseValidSheetCells false
        }

        if (studentsImportParameters == null) {
            var lastCorrectCellRowIndex = -1
            var nameWordsCountInARow = 0
            traverseValidSheetCells(workbook) { sheetIndex, cell ->
                if (cell.stringCellValue.matches(studentNameRegex)) {
                    ++nameWordsCountInARow
                    val currentRowIndex = cell.rowIndex
                    if (lastCorrectCellRowIndex == currentRowIndex) {
                        if (nameWordsCountInARow == 3) {
                            studentsImportParameters = StudentsImportParameters(
                                sheetIndex = sheetIndex,
                                columnIndex = cell.columnIndex - 2,
                                firstRowIndex = currentRowIndex,
                                hasMultipleSheets = hasMultipleSheets,
                                namesAreSeparated = true
                            )
                            return@traverseValidSheetCells true
                        }
                        return@traverseValidSheetCells false
                    } else {
                        if (nameWordsCountInARow in 2..3) {
                            studentsImportParameters = StudentsImportParameters(
                                sheetIndex = sheetIndex,
                                columnIndex = cell.columnIndex,
                                firstRowIndex = currentRowIndex,
                                hasMultipleSheets = hasMultipleSheets,
                                namesAreSeparated = true
                            )
                            return@traverseValidSheetCells true
                        }
                    }
                    lastCorrectCellRowIndex = currentRowIndex
                } else {
                    nameWordsCountInARow = 0
                }
                return@traverseValidSheetCells false
            }
        }

        return if (studentsImportParameters != null) {
            Result.Success(studentsImportParameters)
        } else {
            Result.Error(ExcelStudentImportError.NO_FOUND)
        }
    }

    private fun traverseValidSheetCells(
        workbook: Workbook,
        action: (sheetIndex: Int, cell: Cell) -> Boolean
    ) {
        for (sheetIndex in 0 until workbook.numberOfSheets) {
            val sheet = workbook.getSheetAt(sheetIndex)
            for (rowIndex in sheet.firstRowNum until sheet.lastRowNum.coerceAtLeast(1)) {
                val row = sheet.getRow(rowIndex) ?: return
                for (cellIndex in row.firstCellNum until row.lastCellNum) {
                    val cell = row.getCell(cellIndex)
                    if (cell.cellType == CellType.STRING && cell.stringCellValue.isNotBlank()) {
                        val stop = action.invoke(sheetIndex, cell)
                        if (stop) {
                            cell.rowIndex
                            return
                        }
                    }
                }
            }
        }
    }

    private companion object {
        val studentNameRegex = "^\\s*[а-яА-ЯёЁ-]+\\s*$".toRegex()
        val studentFullNameRegex = "^\\s*(?:[а-яА-ЯёЁ-]+\\s+){1,2}[а-яА-ЯёЁ]+\\s*$".toRegex()
    }
}