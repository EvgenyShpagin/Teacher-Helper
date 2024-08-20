package com.tusur.teacherhelper.presentation.util

import android.app.Activity
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun Activity.getExcelFileFromUri(uri: Uri): File {
    val fileInputStream = contentResolver.openInputStream(uri)!!

    val mimeType = contentResolver.getType(uri)
    val fileExtension = if (mimeType == EXCEL_FILE_NEW_MIME_TYPE) {
        ".xlsx"
    } else {
        ".xls"
    }
    val file = File(cacheDir, "cacheFileImportTable$fileExtension")
    FileOutputStream(file).use { output ->
        val buffer = ByteArray(4 * 1024)
        var read: Int
        while ((fileInputStream.read(buffer).also { read = it }) != -1) {
            output.write(buffer, 0, read)
        }
        output.flush()
    }
    fileInputStream.close()
    return file
}

const val EXCEL_FILE_OLD_MIME_TYPE = "application/vnd.ms-excel"
const val EXCEL_FILE_NEW_MIME_TYPE =
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
val EXCEL_FILE_MIME_TYPES = arrayOf(EXCEL_FILE_OLD_MIME_TYPE, EXCEL_FILE_NEW_MIME_TYPE)