package com.tusur.teacherhelper.presentation.core.base

abstract class TopLevelListUiState<ItemState> : BaseViewModel.UiState {
    open val items: List<ItemState> = emptyList()
    open val isFetching: Boolean = true
    open val isDeleting: Boolean = false
}