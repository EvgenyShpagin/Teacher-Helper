package com.tusur.teacherhelper.presentation.groups

import android.text.InputFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.constraints.InputConstraints
import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.domain.model.error.GroupNumberError
import com.tusur.teacherhelper.domain.usecase.AddGroupToSubjectUseCase
import com.tusur.teacherhelper.domain.usecase.AddNewGroupUseCase
import com.tusur.teacherhelper.domain.usecase.DoesGroupNumberAlreadyExistUseCase
import com.tusur.teacherhelper.domain.usecase.ValidateGroupNumberUseCase
import com.tusur.teacherhelper.presentation.App
import com.tusur.teacherhelper.presentation.basedialog.InputViewModel
import com.tusur.teacherhelper.presentation.model.UiText
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class NewGroupNumberInputViewModel(
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

    companion object {
        val factory = object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NewGroupNumberInputViewModel(
                    addNewGroup = AddNewGroupUseCase(
                        AddGroupToSubjectUseCase(App.module.subjectGroupRepository),
                        App.module.groupRepository
                    ),
                    validateGroupNumber = ValidateGroupNumberUseCase(
                        DoesGroupNumberAlreadyExistUseCase(App.module.groupRepository)
                    )
                ) as T
            }
        }
    }
}