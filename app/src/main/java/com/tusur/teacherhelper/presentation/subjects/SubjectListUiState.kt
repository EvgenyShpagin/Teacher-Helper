package com.tusur.teacherhelper.presentation.subjects

import com.tusur.teacherhelper.presentation.core.base.TopLevelListUiState

data class SubjectListUiState(
    override val isFetching: Boolean = true,
    override val allItems: List<SubjectItemUiState> = emptyList(),
    override val searchedItems: List<SubjectItemUiState> = emptyList()
) : TopLevelListUiState<SubjectItemUiState>()
