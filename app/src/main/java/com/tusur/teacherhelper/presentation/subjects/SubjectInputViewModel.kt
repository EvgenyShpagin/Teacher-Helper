package com.tusur.teacherhelper.presentation.subjects

import android.text.InputFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.constraints.InputConstraints
import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.model.error.SubjectNameError
import com.tusur.teacherhelper.domain.usecase.AddSubjectUseCase
import com.tusur.teacherhelper.domain.usecase.ValidateSubjectNameUseCase
import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.presentation.core.App
import com.tusur.teacherhelper.presentation.core.dialog.InputViewModel
import com.tusur.teacherhelper.presentation.core.model.UiText
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubjectInputViewModel(
    private val validateSubjectName: ValidateSubjectNameUseCase,
    private val addSubject: AddSubjectUseCase
) : InputViewModel() {

    val inputFilter = InputFilter.LengthFilter(InputConstraints.MAX_SUBJECT_NAME_LENGTH)

    override fun send(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.TryAdd -> when (val result = validateSubjectName(event.text)) {
                    is Result.Error -> handleError(result.error)
                    is Result.Success -> handleSuccess(result.data)
                }

                is Event.Input -> {
                    if (uiState.value.error != null) {
                        mutableUiState.update { it.copy(error = null) }
                    }
                }
            }
        }
    }

    private fun handleError(error: SubjectNameError) {
        mutableUiState.update {
            it.copy(
                error = when (error) {
                    SubjectNameError.ALREADY_EXISTS ->
                        UiText.Resource(R.string.subject_already_exists_error)

                    else -> UiText.Resource(R.string.incorrect_subject_name_error)
                }
            )
        }
    }

    private suspend fun handleSuccess(subject: Subject) {
        addSubject(subject)
        mutableUiState.update { it.copy(isSavedSuccessfully = true) }
    }

    companion object {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SubjectInputViewModel(
                    ValidateSubjectNameUseCase(App.module.subjectRepository),
                    AddSubjectUseCase(App.module.subjectRepository)
                ) as T
            }
        }
    }
}
