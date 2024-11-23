package com.tusur.teacherhelper.presentation.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.usecase.GetSubjectListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectListViewModel @Inject constructor(
    private val getSubjectList: GetSubjectListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()


    fun send(event: Event) {
        viewModelScope.launch {
            when (event) {
                Event.Fetch -> {
                    _uiState.update { it.copy(isFetching = true) }
                    getSubjectList().flowOn(Dispatchers.IO).collect { newList ->
                        _uiState.update { it.copy(itemsUiState = newList, isFetching = false) }
                    }
                }
            }
        }
    }


    data class UiState(
        val isFetching: Boolean = true,
        val itemsUiState: List<SubjectItemUiState> = emptyList()
    ) {
        val listIsEmpty = itemsUiState.isEmpty()
    }

    sealed interface Event {
        data object Fetch : Event
    }
}

// Because item UI needs the same as Domain model
typealias SubjectItemUiState = Subject