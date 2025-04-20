package com.tusur.teacherhelper.presentation.globaltopic

import com.tusur.teacherhelper.presentation.core.base.TopLevelListUiState

data class GlobalTopicListUiState(
    override val isFetching: Boolean = true,
    override val allItems: List<GlobalTopicUiState> = emptyList(),
    override val searchedItems: List<GlobalTopicUiState> = emptyList(),
) : TopLevelListUiState<GlobalTopicUiState>()