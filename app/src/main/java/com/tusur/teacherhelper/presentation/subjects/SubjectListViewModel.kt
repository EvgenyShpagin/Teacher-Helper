package com.tusur.teacherhelper.presentation.subjects

import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.usecase.DeleteSubjectWithTopicsUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectListUseCase
import com.tusur.teacherhelper.domain.usecase.SearchSubjectUseCase
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectListViewModel @Inject constructor(
    private val getSubjectList: GetSubjectListUseCase,
    private val searchSubject: SearchSubjectUseCase,
    private val deleteSubjectWithTopics: DeleteSubjectWithTopicsUseCase
) : TopLevelListViewModel<SubjectListUiState, Nothing>(
    initialUiState = SubjectListUiState()
) {
    override fun onEvent(event: Event) {
        when (event) {
            Event.Fetch -> fetch()
            is Event.Search -> search(event.query)
            is Event.TryDelete -> delete(event.itemId)
        }
    }

    private fun fetch() {
        viewModelScope.launch {
            updateState { it.copy(isFetching = true) }
            getSubjectList().collect { newList ->
                updateState {
                    it.copy(
                        allItems = newList,
                        searchedItems = newList,
                        isFetching = false
                    )
                }
            }
        }
    }

    private fun search(query: String) {
        viewModelScope.launch {
            updateState { state -> state.copy(isFetching = true) }
            val searchedSubjects = searchSubject("%$query%")
            updateState { state ->
                state.copy(isFetching = false, searchedItems = searchedSubjects)
            }
        }
    }

    private fun delete(subjectId: Int) {
        viewModelScope.launch {
            deleteSubjectWithTopics(subjectId)
        }
    }
}
