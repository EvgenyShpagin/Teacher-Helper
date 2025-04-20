package com.tusur.teacherhelper.presentation.topictype

import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.model.error.TopicTypeDeleteError
import com.tusur.teacherhelper.domain.usecase.DeleteTopicTypeUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicTypesUseCase
import com.tusur.teacherhelper.domain.usecase.SearchTopicTypeUseCase
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel.Event.Fetch
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel.Event.Search
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel.Event.TryDelete
import com.tusur.teacherhelper.presentation.core.model.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicTypeListViewModel @Inject constructor(
    private val getTopicTypesUseCase: GetTopicTypesUseCase,
    private val searchTopicType: SearchTopicTypeUseCase,
    private val deleteTopicType: DeleteTopicTypeUseCase
) : TopLevelListViewModel<TopicTypeListUiState, TopicTypeListUiEffect>(
    initialUiState = TopicTypeListUiState()
) {
    override fun onEvent(event: Event) {
        when (event) {
            Fetch -> fetch()
            is TryDelete -> delete(event.itemId)
            is Search -> search(event.query)
        }
    }

    private fun fetch() {
        viewModelScope.launch {
            getTopicTypesUseCase().collect { topicTypes ->
                val uiTopicTypes = topicTypes.map { it.toUiItem() }
                updateState { state ->
                    state.copy(
                        isFetching = false,
                        allItems = uiTopicTypes,
                        searchedItems = uiTopicTypes
                    )
                }
            }
        }
    }

    private fun delete(typeId: Int) {
        viewModelScope.launch {
            deleteTopicType(typeId = typeId).onFailure { error ->
                triggerEffect(
                    when (error) {
                        TopicTypeDeleteError.CANNOT_DELETE_BASE_TYPES ->
                            TopicTypeListUiEffect.FailedToDeleteBaseTopicType

                        TopicTypeDeleteError.USED_BY_SOME_TOPICS ->
                            TopicTypeListUiEffect.FailedToDeleteUsedTopicType
                    }
                )
            }
        }
    }

    private fun search(query: String) {
        updateState { it.copy(isFetching = true) }
        viewModelScope.launch {
            val searchedTypes = searchTopicType("%$query%")
                .map { topicType -> topicType.toUiItem() }
            updateState {
                it.copy(
                    isFetching = false,
                    searchedItems = searchedTypes
                )
            }
        }
    }

    private fun TopicType.toUiItem() = TopicTypeItemUiState(
        typeId = id, name = UiText.Dynamic(name)
    )
}
