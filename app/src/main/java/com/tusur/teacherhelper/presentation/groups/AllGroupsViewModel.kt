package com.tusur.teacherhelper.presentation.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.usecase.DeleteGroupUseCase
import com.tusur.teacherhelper.domain.usecase.GetAllGroupsUseCase
import com.tusur.teacherhelper.domain.usecase.IsGroupAssociatedToAnySubjectUseCase
import com.tusur.teacherhelper.domain.usecase.SearchGroupUseCase
import com.tusur.teacherhelper.presentation.core.App
import com.tusur.teacherhelper.presentation.subjectdetails.GroupItemUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AllGroupsViewModel(
    private val getAllGroups: GetAllGroupsUseCase,
    private val searchGroup: SearchGroupUseCase,
    private val deleteGroup: DeleteGroupUseCase,
    private val isGroupAssociatedToAnySubject: IsGroupAssociatedToAnySubjectUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAllGroups().flowOn(Dispatchers.IO).collect { groups ->
                _uiState.update { uiState ->
                    uiState.copy(
                        groupsUiState = groups.map { it.toItemUiState() },
                        isFetching = false
                    )
                }
            }
        }
    }


    fun searchGroup(searchQuery: String) {
        _uiState.update { it.copy(isFetching = true) }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isFetching = false,
                    groupsUiState = searchGroup.invoke(searchQuery)
                        .map { group -> group.toItemUiState() }
                )
            }
        }
    }

    suspend fun canGroupBeDeleted(groupId: Int): Boolean {
        return !isGroupAssociatedToAnySubject(groupId)
    }

    fun stopDelete() {
        _uiState.update { it.copy(isDeleting = false) }
    }

    fun startDelete() {
        _uiState.update { it.copy(isDeleting = true) }
    }

    fun deleteGroup(groupId: Int) {
        viewModelScope.launch { deleteGroup.invoke(groupId) }
    }

    data class UiState(
        val isFetching: Boolean = true,
        val groupsUiState: List<GroupItemUiState> = emptyList(),
        val isDeleting: Boolean = false,
    )

    private fun Group.toItemUiState() = GroupItemUiState(id, number)

    companion object {

        val factory = object : ViewModelProvider.Factory {

            private val getAllGroups = GetAllGroupsUseCase(App.module.groupRepository)
            private val searchGroup = SearchGroupUseCase(App.module.groupRepository)
            private val deleteGroup = DeleteGroupUseCase(App.module.groupRepository)
            private val isGroupAssociatedToAnyTopic =
                IsGroupAssociatedToAnySubjectUseCase(App.module.groupRepository)

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AllGroupsViewModel(
                    getAllGroups,
                    searchGroup,
                    deleteGroup,
                    isGroupAssociatedToAnyTopic
                ) as T
            }
        }
    }
}