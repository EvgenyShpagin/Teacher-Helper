package com.tusur.teacherhelper.presentation.core.base

abstract class TopLevelListUiState<ItemState> : BaseViewModel.UiState {
    abstract val allItems: List<ItemState>
    abstract val searchedItems: List<ItemState>
    abstract val isFetching: Boolean
    abstract val isDeleting: Boolean
}