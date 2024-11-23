package com.tusur.teacherhelper.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.StudentsImportParameters
import com.tusur.teacherhelper.domain.model.error.ExcelStudentImportError
import com.tusur.teacherhelper.domain.usecase.AddStudentsUseCase
import com.tusur.teacherhelper.domain.usecase.GetStudentsFromExcelFileUseCase
import com.tusur.teacherhelper.domain.usecase.GetSuggestedExcelFileParametersUseCase
import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.presentation.core.model.UiText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File


@HiltViewModel(assistedFactory = GroupImportViewModel.Factory::class)
class GroupImportViewModel @AssistedInject constructor(
    @Assisted private val groupId: Int,
    @Assisted private val excelFile: File,
    private val getSuggestedExcelFileParameters: GetSuggestedExcelFileParametersUseCase,
    private val getStudentsFromExcelFile: GetStudentsFromExcelFileUseCase,
    private val addStudents: AddStudentsUseCase
) : ViewModel() {

    private var importParams: StudentsImportParameters? = null

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()


    fun fetch() {
        viewModelScope.launch {
            when (val suggestedExcelFileParameters = getSuggestedExcelFileParameters(excelFile)) {
                is Result.Error -> handleError(suggestedExcelFileParameters.error)
                is Result.Success -> _uiState.update {
                    importParams = suggestedExcelFileParameters.data
                    UiState(
                        hasMultipleSheets = importParams!!.hasMultipleSheets,
                        sheetIndex = (importParams!!.sheetIndex + 1).toString(),
                        firstRowIndex = (importParams!!.firstRowIndex + 1).toString(),
                        firstColumnIndex = (importParams!!.columnIndex + 1).toString(),
                        areNamesSeparated = importParams!!.namesAreSeparated
                    )
                }
            }

        }
    }

    fun send(event: Event) {
        when (event) {
            Event.Confirm -> addStudents()
            is Event.InputColumn -> updateColumnIndex(event.number)
            is Event.InputRow -> updateRowIndex(event.number)
            is Event.InputSheet -> updateSheetIndex(event.number)
        }
    }

    private fun updateColumnIndex(number: String) {
        _uiState.update {
            it.copy(
                firstColumnIndex = number,
                isAllInputCorrect = it.firstRowIndex.isNotBlank()
                        && it.sheetIndex.isNotBlank()
                        && number.isNotBlank()
            )
        }

    }

    private fun updateRowIndex(number: String) {
        _uiState.update {
            it.copy(
                firstRowIndex = number,
                isAllInputCorrect = number.isNotBlank()
                        && it.sheetIndex.isNotBlank()
                        && it.firstColumnIndex.isNotBlank()
            )
        }
    }

    private fun updateSheetIndex(number: String) {
        _uiState.update {
            it.copy(
                sheetIndex = number,
                isAllInputCorrect = it.firstRowIndex.isNotBlank()
                        && it.sheetIndex.isNotBlank()
                        && it.firstColumnIndex.isNotBlank()
            )
        }
    }

    private fun addStudents() {
        viewModelScope.launch {
            when (val result = getStudentsFromExcelFile(importParams!!, excelFile)) {
                is Result.Error -> handleError(result.error)
                is Result.Success -> addStudents(result.data, groupId)
            }

        }
    }

    private fun handleError(error: ExcelStudentImportError) {
        _uiState.update {
            it.copy(
                errorMessage = when (error) {
                    ExcelStudentImportError.FILE_NOT_CHOSEN -> UiText.Resource(R.string.group_import_file_no_file_error)
                    ExcelStudentImportError.WRONG_FILE_FORMAT -> UiText.Resource(R.string.group_import_file_wrong_format_error)
                    ExcelStudentImportError.NO_FOUND -> UiText.Resource(R.string.group_import_file_no_students_error)
                }
            )

        }
    }

    sealed interface Event {
        data object Confirm : Event
        data class InputColumn(val number: String) : Event
        data class InputSheet(val number: String) : Event
        data class InputRow(val number: String) : Event
    }

    data class UiState(
        val hasMultipleSheets: Boolean = true,
        val sheetIndex: String = "0",
        val firstRowIndex: String = "0",
        val firstColumnIndex: String = "0",
        val areNamesSeparated: Boolean = false,
        val isAllInputCorrect: Boolean = true,
        val errorMessage: UiText? = null
    )

    @AssistedFactory
    interface Factory {
        fun create(groupId: Int, excelFile: File): GroupImportViewModel
    }
}