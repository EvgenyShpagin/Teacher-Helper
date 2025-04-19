package com.tusur.teacherhelper.presentation.groups

import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.usecase.DeleteGroupUseCase
import com.tusur.teacherhelper.domain.usecase.GetAllGroupsUseCase
import com.tusur.teacherhelper.domain.usecase.IsGroupAssociatedToAnySubjectUseCase
import com.tusur.teacherhelper.domain.usecase.SearchGroupUseCase
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val getAllGroups: GetAllGroupsUseCase,
    private val searchGroup: SearchGroupUseCase,
    private val deleteGroup: DeleteGroupUseCase,
    private val isGroupAssociatedToAnySubject: IsGroupAssociatedToAnySubjectUseCase
) : TopLevelListViewModel<GroupsUiState, GroupsEffect>(
    initialUiState = GroupsUiState()
) {

    override fun onEvent(event: Event) {
        when (event) {
            Event.BeginDelete -> startDelete()
            Event.Fetch -> fetch()
            is Event.Search -> searchGroup(event.query)
            Event.StopDelete -> stopDelete()
            is Event.TryDelete -> deleteGroup(event.itemId)
        }
    }

    private fun fetch() {
        viewModelScope.launch {
            getAllGroups().flowOn(Dispatchers.IO).collect { groups ->
                val uiGroups = groups.map { it.toItemUiState() }
                updateState { uiState ->
                    uiState.copy(
                        allItems = uiGroups,
                        searchedItems = uiGroups,
                        isFetching = false
                    )
                }
            }
        }
    }

    private fun searchGroup(searchQuery: String) {
        updateState { it.copy(isFetching = true) }
        viewModelScope.launch {
            val items = searchGroup.invoke("%$searchQuery%")
                .map { group -> group.toItemUiState() }
            updateState {
                it.copy(isFetching = false, searchedItems = items)
            }
        }
    }

    private fun stopDelete() {
        updateState { it.copy(isDeleting = false) }
    }

    private fun startDelete() {
        updateState { it.copy(isDeleting = true) }
    }

    private fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            if (isGroupAssociatedToAnySubject(groupId)) {
                triggerEffect(GroupsEffect.FailedToDeleteGroup)
            } else {
                deleteGroup.invoke(groupId)
            }
        }
    }

    private fun Group.toItemUiState() = GroupItemUiState(id, number)
}