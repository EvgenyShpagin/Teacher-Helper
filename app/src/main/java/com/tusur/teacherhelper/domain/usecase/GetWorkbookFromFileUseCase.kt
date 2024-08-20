package com.tusur.teacherhelper.domain.usecase

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

class GetWorkbookFromFileUseCase {

    private var lastWorkbookFilename: String? = null
    private var lastWorkbook: Workbook? = null

    operator fun invoke(file: File?): Workbook? {
        if (file == null) {
            return null
        }
        if (file.name == lastWorkbookFilename) {
            return lastWorkbook
        }
        lastWorkbookFilename = file.name
        return when (file.extension.lowercase()) {
            "xlsx" -> XSSFWorkbook(file.inputStream())
            "xls" -> HSSFWorkbook(file.inputStream())
            else -> return null
        }.also { lastWorkbook = it }
    }
}