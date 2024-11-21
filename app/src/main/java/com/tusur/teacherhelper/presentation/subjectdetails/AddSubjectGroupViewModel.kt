package com.tusur.teacherhelper.presentation.subjectdetails

import android.text.InputFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.constraints.InputConstraints
import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.domain.model.error.GroupNumberError
import com.tusur.teacherhelper.domain.usecase.AddGroupToSubjectUseCase
import com.tusur.teacherhelper.domain.usecase.AddNewGroupUseCase
import com.tusur.teacherhelper.domain.usecase.DoesGroupNumberAlreadyExistUseCase
import com.tusur.teacherhelper.domain.usecase.GetAvailableGroupsToAddUseCase
import com.tusur.teacherhelper.domain.usecase.SearchInListUseCase
import com.tusur.teacherhelper.domain.usecase.ValidateGroupNumberUseCase
import com.tusur.teacherhelper.presentation.App
import com.tusur.teacherhelper.presentation.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddSubjectGroupViewModel(
    private val subjectId: Int,
    private val getAvailableGroupsToAdd: GetAvailableGroupsToAddUseCase,
    private val addGroup: AddGroupToSubjectUseCase,
    private val searchInList: SearchInListUseCase,
    private val addNewGroup: AddNewGroupUseCase,
    private val validateGroupNumber: ValidateGroupNumberUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    val inputFilter = InputFilter.LengthFilter(InputConstraints.MAX_GROUP_NUMBER_LENGTH)

    private var allAvailableGroups = emptyList<Group>()
    private var allAvailableGroupUiItems = emptyList<SimpleGroupItemUiState>()

    init {
        viewModelScope.launch {
            allAvailableGroups = getAvailableGroupsToAdd(subjectId)
            allAvailableGroupUiItems = allAvailableGroups.map { it.toUiItem() }
            _uiState.update { it.copy(availableGroups = allAvailableGroupUiItems) }
        }
    }

    fun send(event: Event) {
        viewModelScope.launch {
            if (event is Event.Input) {
                handleInput(event.groupNumber)
            } else if (event == Event.TryAddNewGroup) {
                val checkedGroup = uiState.value.availableGroups.find { it.isChecked }
                if (checkedGroup != null) {
                    addGroup(checkedGroup.id)
                    _uiState.update { it.copy(groupIsAdded = true) }
                } else if (uiState.value.errorText == null) {
                    _uiState.update {
                        it.copy(justCreatedGroupId = addNewGroup(it.groupNumber))
                    }
                }
            }
        }
    }

    private fun checkGroup(groupId: Int) {
        val currentGroups = uiState.value.availableGroups
        _uiState.update { uiState ->
            uiState.copy(
                availableGroups = currentGroups.map {
                    if (it.id == groupId) {
                        it.copy(isChecked = !it.isChecked)
                    } else if (it.isChecked) {
                        it.copy(isChecked = false)
                    } else {
                        it
                    }
                }
            )
        }
    }

    private suspend fun handleInput(number: String) {
        val currentChecked = uiState.value.availableGroups.find { it.isChecked }
        val found = searchInList(number, allAvailableGroupUiItems) { it.number }
        val searchedOne = found.find { it.number == number }
        _uiState.update { uiState ->
            uiState.copy(
                groupNumber = number,
                availableGroups = if (searchedOne != null) {
                    found.map {
                        when (it.id) {
                            searchedOne.id -> searchedOne.copy(isChecked = true)
                            currentChecked?.id -> it.copy(isChecked = false)
                            else -> it
                        }
                    }
                } else if (currentChecked != null) {
                    found.map {
                        if (it.id == currentChecked.id) {
                            currentChecked
                        } else {
                            it
                        }
                    }
                } else {
                    found
                }
            )
        }
        handleNumberValidation(validateGroupNumber(number, allAvailableGroups))
    }

    private suspend fun addGroup(groupId: Int) {
        addGroup.invoke(subjectId = subjectId, groupId = groupId)
    }

    private suspend fun addNewGroup(groupNumber: String): Int {
        return addNewGroup.invoke(subjectId = subjectId, groupNumber = groupNumber)
    }

    private fun handleNumberValidation(validationResult: Result<Unit, GroupNumberError>) {
        _uiState.update {
            it.copy(
                errorText = when (validationResult) {
                    is Result.Error -> {
                        when (validationResult.error) {
                            GroupNumberError.INCORRECT ->
                                UiText.Resource(R.string.group_number_incorrect)

                            GroupNumberError.ALREADY_EXISTS ->
                                UiText.Resource(R.string.group_number_already_exists)

                            GroupNumberError.EMPTY -> null
                        }
                    }

                    is Result.Success -> null
                }
            )
        }
    }

    private fun Group.toUiItem() =
        SimpleGroupItemUiState(id, number, false) { checkGroup(id) }

    sealed interface Event {
        data object TryAddNewGroup : Event
        data class Input(val groupNumber: String) : Event
    }

    data class UiState(
        val groupNumber: String = "",
        val justCreatedGroupId: Int? = null,
        val availableGroups: List<SimpleGroupItemUiState> = emptyList(),
        val groupIsAdded: Boolean = false,
        val errorText: UiText? = null
    ) {
        val anyChecked: Boolean get() = availableGroups.any { it.isChecked }
    }

    companion object {
        fun factory(subjectId: Int) = object : ViewModelProvider.Factory {

            private val getAvailableGroupsToAdd = GetAvailableGroupsToAddUseCase(
                App.module.groupRepository,
                App.module.subjectGroupRepository
            )
            private val addGroup = AddGroupToSubjectUseCase(App.module.subjectGroupRepository)
            private val searchInList = SearchInListUseCase()
            private val addNewGroup = AddNewGroupUseCase(addGroup, App.module.groupRepository)
            private val getGroupIdByNumber =
                DoesGroupNumberAlreadyExistUseCase(App.module.groupRepository)
            private val validateGroupNumber = ValidateGroupNumberUseCase(getGroupIdByNumber)

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddSubjectGroupViewModel(
                    subjectId,
                    getAvailableGroupsToAdd,
                    addGroup,
                    searchInList,
                    addNewGroup,
                    validateGroupNumber
                ) as T
            }
        }
    }
}

data class SimpleGroupItemUiState(
    val id: Int,
    val number: String,
    val isChecked: Boolean,
    val onCheck: () -> Unit
)