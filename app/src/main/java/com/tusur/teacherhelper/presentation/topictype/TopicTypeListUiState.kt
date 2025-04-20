package com.tusur.teacherhelper.presentation.topictype

import com.tusur.teacherhelper.presentation.core.base.TopLevelListUiState

data class TopicTypeListUiState(
    override val isFetching: Boolean = true,
    override val allItems: List<TopicTypeItemUiState> = emptyList(),
    override val searchedItems: List<TopicTypeItemUiState> = emptyList()
) : TopLevelListUiState<TopicTypeItemUiState>()
