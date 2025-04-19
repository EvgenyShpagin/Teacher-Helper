package com.tusur.teacherhelper.presentation.core.base

abstract class TopLevelListViewModel<State : TopLevelListUiState<*>, Effect : TopLevelListUiEffect>(
    initialUiState: State
) : BaseViewModel<State, TopLevelListViewModel.Event, Effect>(initialUiState) {

    sealed interface Event : UiEvent {
        data object Fetch : Event
        data class Search(val query: String) : Event
        data class TryDelete(val itemId: Int) : Event
    }
}