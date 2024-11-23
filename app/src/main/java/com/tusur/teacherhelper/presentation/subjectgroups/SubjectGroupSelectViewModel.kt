package com.tusur.teacherhelper.presentation.subjectgroups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.usecase.GetSubjectByIdUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectNotEmptyGroupsUseCase
import com.tusur.teacherhelper.domain.usecase.SearchSubjectGroupUseCase
import com.tusur.teacherhelper.presentation.subjectdetails.SimpleGroupItemUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel(assistedFactory = SubjectGroupSelectViewModel.Factory::class)
class SubjectGroupSelectViewModel @AssistedInject constructor(
    @Assisted private val subjectId: Int,
    @Assisted private val shouldBeAllChecked: Boolean,
    private val getSubject: GetSubjectByIdUseCase,
    private val getSubjectNotEmptyGroups: GetSubjectNotEmptyGroupsUseCase,
    private val searchGroup: SearchSubjectGroupUseCase,
) : ViewModel() {

    private val checkedGroupIds = mutableListOf<Int>()

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private lateinit var subject: Subject

    fun fetchGroups() {
        viewModelScope.launch {
            subject = getSubject(subjectId)
            _uiState.update { state ->
                state.copy(
                    groupsUiState = getSubjectNotEmptyGroups(subject.id)
                        .map { it.toUiItem(shouldBeAllChecked) }
                        .onEach { checkedGroupIds.add(it.id) },
                    wasFetched = true
                )
            }
        }
    }

    fun searchGroup(searchQuery: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    groupsUiState = searchGroup.invoke(subject, searchQuery).map { group ->
                        group.toUiItem(isChecked = group.id in checkedGroupIds)
                    }
                )
            }
        }
    }

    fun getCheckedItemIds(): List<Int> {
        return uiState.value.groupsUiState.map { it.id }
    }

    private fun checkItem(id: Int) {
        _uiState.update { state ->
            state.copy(
                groupsUiState = state.groupsUiState.map {
                    if (it.id == id) {
                        it.copy(isChecked = !it.isChecked)
                    } else {
                        it
                    }
                }
            )
        }
        checkedGroupIds.remove(id).also { isRemoved ->
            if (!isRemoved) {
                checkedGroupIds.add(id)
            }
        }
    }

    private fun Group.toUiItem(isChecked: Boolean) = SimpleGroupItemUiState(
        id = id,
        number = number,
        isChecked = isChecked,
        onCheck = { checkItem(id) }
    )

    fun stopSearch() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    groupsUiState = getSubjectNotEmptyGroups(subject.id)
                        .map { it.toUiItem(it.id in checkedGroupIds) }
                )
            }
        }
    }

    data class UiState(
        val groupsUiState: List<SimpleGroupItemUiState> = emptyList(),
        val wasFetched: Boolean = false
    ) {
        val hasChecked get() = groupsUiState.find { it.isChecked } != null
    }

    @AssistedFactory
    interface Factory {
        fun create(subjectId: Int, shouldBeAllChecked: Boolean): SubjectGroupSelectViewModel
    }
}