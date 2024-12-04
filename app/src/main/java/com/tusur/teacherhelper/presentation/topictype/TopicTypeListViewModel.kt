package com.tusur.teacherhelper.presentation.topictype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.model.error.TopicTypeDeleteError
import com.tusur.teacherhelper.domain.usecase.DeleteTopicTypeUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicTypesUseCase
import com.tusur.teacherhelper.domain.usecase.SearchTopicTypeUseCase
import com.tusur.teacherhelper.presentation.core.model.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicTypeListViewModel @Inject constructor(
    private val getTopicTypesUseCase: GetTopicTypesUseCase,
    private val searchTopicType: SearchTopicTypeUseCase,
    private val deleteTopicType: DeleteTopicTypeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _onetimeEvents = Channel<OnetimeEvent>()
    val onetimeEvent = _onetimeEvents.receiveAsFlow()


    fun send(event: Event) {
        when (event) {
            Event.Fetch -> fetch()
            Event.BeginDelete -> beginDelete()
            Event.StopDelete -> stopDelete()
            is Event.TryDelete -> delete(event.typeId)
            is Event.Search -> search(event.query)
        }
    }

    private fun fetch() {
        viewModelScope.launch {
            getTopicTypesUseCase().collect { topicTypes ->
                _uiState.update { state ->
                    state.copy(
                        isFetching = false,
                        typesUiState = topicTypes.map { it.toUiItem() }
                    )
                }
            }
        }
    }

    private fun beginDelete() {
        _uiState.update { it.copy(isDeleting = true) }
    }

    private fun stopDelete() {
        _uiState.update { it.copy(isDeleting = false) }
    }

    private fun delete(typeId: Int) {
        viewModelScope.launch {
            deleteTopicType(typeId = typeId).onFailure { error ->
                _onetimeEvents.send(
                    when (error) {
                        TopicTypeDeleteError.CANNOT_DELETE_BASE_TYPES ->
                            OnetimeEvent.FailedToDeleteBaseTopicType

                        TopicTypeDeleteError.USED_BY_SOME_TOPICS ->
                            OnetimeEvent.FailedToDeleteUsedTopicType
                    }
                )
            }.onSuccess {
                if (uiState.value.typesUiState.isEmpty()) {
                    stopDelete()
                }
            }
        }
    }

    private fun search(query: String) {
        _uiState.update { it.copy(isFetching = true) }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isFetching = false,
                    typesUiState = searchTopicType(query)
                        .map { topicType -> topicType.toUiItem() }
                )
            }
        }
    }

    private fun TopicType.toUiItem() = TopicTypeItemUiState(
        typeId = id, name = UiText.Dynamic(name)
    )

    data class UiState(
        val isFetching: Boolean = true,
        val typesUiState: List<TopicTypeItemUiState> = emptyList(),
        val isDeleting: Boolean = false
    )

    sealed interface Event {
        data object Fetch : Event
        data object BeginDelete : Event
        data object StopDelete : Event
        data class Search(val query: String) : Event
        data class TryDelete(val typeId: Int) : Event
    }

    sealed interface OnetimeEvent {
        data object FailedToDeleteBaseTopicType : OnetimeEvent
        data object FailedToDeleteUsedTopicType : OnetimeEvent
    }
}

data class TopicTypeItemUiState(
    val typeId: Int,
    val name: UiText
)