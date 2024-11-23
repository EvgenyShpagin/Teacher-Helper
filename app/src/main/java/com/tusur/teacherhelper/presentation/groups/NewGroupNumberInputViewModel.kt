package com.tusur.teacherhelper.presentation.groups

import android.text.InputFilter
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.constraints.InputConstraints
import com.tusur.teacherhelper.domain.model.error.GroupNumberError
import com.tusur.teacherhelper.domain.usecase.AddNewGroupUseCase
import com.tusur.teacherhelper.domain.usecase.ValidateGroupNumberUseCase
import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.presentation.core.dialog.InputViewModel
import com.tusur.teacherhelper.presentation.core.model.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NewGroupNumberInputViewModel @Inject constructor(
    private val addNewGroup: AddNewGroupUseCase,
    private val validateGroupNumber: ValidateGroupNumberUseCase
) : InputViewModel() {

    val inputFilter = InputFilter.LengthFilter(InputConstraints.MAX_GROUP_NUMBER_LENGTH)

    override fun send(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.TryAdd -> validateGroupNumber(groupNumber = event.text)
                    .also { validationResult ->
                        handleNumberValidation(
                            groupNumber = event.text,
                            validationResult = validationResult
                        )
                    }

                is Event.Input -> {
                    if (uiState.value.error != null) {
                        mutableUiState.update { it.copy(error = null) }
                    }
                }
            }
        }
    }

    private suspend fun handleNumberValidation(
        groupNumber: String,
        validationResult: Result<Unit, GroupNumberError>
    ) {
        mutableUiState.update {
            when (validationResult) {
                is Result.Success -> it.copy(savedItemId = addNewGroup(groupNumber), error = null)

                is Result.Error -> it.copy(
                    error = when (validationResult.error) {
                        GroupNumberError.INCORRECT ->
                            UiText.Resource(R.string.group_number_incorrect)

                        GroupNumberError.ALREADY_EXISTS ->
                            UiText.Resource(R.string.group_number_already_exists)

                        GroupNumberError.EMPTY -> null
                    }
                )
            }
        }
    }
}