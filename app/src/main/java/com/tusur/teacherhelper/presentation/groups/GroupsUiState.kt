package com.tusur.teacherhelper.presentation.groups

import com.tusur.teacherhelper.presentation.core.base.TopLevelListUiState

data class GroupsUiState(
    override val isFetching: Boolean = true,
    override val allItems: List<GroupItemUiState> = emptyList(),
    override val searchedItems: List<GroupItemUiState> = emptyList()
) : TopLevelListUiState<GroupItemUiState>()