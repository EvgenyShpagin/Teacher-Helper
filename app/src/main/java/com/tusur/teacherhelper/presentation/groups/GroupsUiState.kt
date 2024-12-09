package com.tusur.teacherhelper.presentation.groups

import com.tusur.teacherhelper.presentation.core.base.TopLevelListUiState

data class GroupsUiState(
    override val isFetching: Boolean = true,
    override val items: List<GroupItemUiState> = emptyList(),
    override val isDeleting: Boolean = false,
) : TopLevelListUiState<GroupItemUiState>()