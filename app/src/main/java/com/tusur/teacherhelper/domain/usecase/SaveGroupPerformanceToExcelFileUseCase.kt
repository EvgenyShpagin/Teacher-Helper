package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.TableContent
import com.tusur.teacherhelper.domain.util.map
import com.tusur.teacherhelper.presentation.core.util.EXCEL_FILE_NEW_MIME_TYPE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import javax.inject.Inject


typealias Mime = String

class SaveGroupPerformanceToExcelFileUseCase @Inject constructor() {

    suspend operator fun <T> invoke(
        fileOutputStream: OutputStream,
        tableContent: TableContent<T>,
        stringConverter: (T) -> String
    ): Mime {
        tableContent.map { stringConverter(it) }
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet()

        val cellStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
        }

        writeLabels(tableContent, sheet, stringConverter, cellStyle)
        writeContent(tableContent, sheet, stringConverter, cellStyle)

        sheet.setColumnWidth(0, 255 * 16)

        withContext(Dispatchers.IO) {
            workbook.write(fileOutputStream)
            fileOutputStream.close()
        }
        joinAll()
        return EXCEL_FILE_NEW_MIME_TYPE
    }

    private fun <T> writeLabels(
        tableContent: TableContent<T>,
        sheet: Sheet,
        stringConverter: (T) -> String,
        cellStyle: CellStyle
    ) {
        if (tableContent.columnLabels.isEmpty()) return
        val row = sheet.createRow(0)
        tableContent.columnLabels.forEachIndexed { index, item ->
            val cell = row.createCell(index, CellType.STRING)
            cell.cellStyle = cellStyle
            cell.setCellValue(stringConverter(item))
        }
    }

    private fun <T> writeContent(
        tableContent: TableContent<T>,
        sheet: Sheet,
        stringConverter: (T) -> String,
        cellStyle: CellStyle
    ) {
        val hasLabels = tableContent.columnLabels.isNotEmpty()
        var prevRowIndex = if (hasLabels) {
            0
        } else {
            -1
        }
        var row: Row? = null

        tableContent.forEach { item, rowIndex, colIndex ->
            val absoluteRowIndex = if (hasLabels) {
                rowIndex + 1
            } else {
                rowIndex
            }
            if (prevRowIndex != absoluteRowIndex) {
                row = sheet.createRow(absoluteRowIndex)
            }
            val cell = row!!.createCell(colIndex, CellType.STRING)

            if (colIndex != 0) {
                cell.cellStyle = cellStyle
            }
            cell.setCellValue(stringConverter(item))
            prevRowIndex = absoluteRowIndex
        }
    }
}