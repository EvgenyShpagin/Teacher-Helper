package com.tusur.teacherhelper.presentation.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.usecase.GetSubjectListUseCase
import com.tusur.teacherhelper.presentation.core.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubjectListViewModel(private val getSubjectList: GetSubjectListUseCase) : ViewModel() {

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

    companion object {
        val factory = object : ViewModelProvider.Factory {
            private val getSubjectListUseCase = GetSubjectListUseCase(App.module.subjectRepository)

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SubjectListViewModel(getSubjectListUseCase) as T
            }
        }
    }
}

// Because item UI needs the same as Domain model
typealias SubjectItemUiState = Subject