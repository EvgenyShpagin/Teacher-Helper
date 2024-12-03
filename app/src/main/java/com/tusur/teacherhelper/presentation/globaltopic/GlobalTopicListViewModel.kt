package com.tusur.teacherhelper.presentation.globaltopic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.error.DeleteTopicError.OthersDependOnTopicDeadline
import com.tusur.teacherhelper.domain.usecase.DeleteTopicUseCase
import com.tusur.teacherhelper.domain.usecase.GetGlobalTopicsUseCase
import com.tusur.teacherhelper.domain.usecase.SearchGlobalTopicUseCase
import com.tusur.teacherhelper.domain.util.GLOBAL_TOPICS_SUBJECT_ID
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.globaltopic.GlobalTopicListViewModel.OnetimeEvent.FailedToDeleteDeadline
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

@HiltViewModel(assistedFactory = GlobalTopicListViewModel.Factory::class)
class GlobalTopicListViewModel @AssistedInject constructor(
    @Assisted private val locale: Locale,
    private val getGlobalTopics: GetGlobalTopicsUseCase,
    private val searchGlobalTopic: SearchGlobalTopicUseCase,
    private val deleteTopic: DeleteTopicUseCase
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
            is Event.TryDelete -> delete(event.topicId)
            is Event.Search -> search(event.query)
        }
    }

    private fun fetch() {
        viewModelScope.launch {
            getGlobalTopics().collect { globalTopics ->
                _uiState.update { state ->
                    state.copy(
                        isFetching = false,
                        topicsUiState = globalTopics.map { it.toUiItem() }
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

    private fun delete(topicId: Int) {
        viewModelScope.launch {
            deleteTopic(
                topicId = topicId,
                subjectId = GLOBAL_TOPICS_SUBJECT_ID
            ).onFailure { error ->
                when (error) {
                    OthersDependOnTopicDeadline -> _onetimeEvents.send(FailedToDeleteDeadline)
                }
            }.onSuccess {
                if (uiState.value.topicsUiState.isEmpty()) {
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
                    topicsUiState = searchGlobalTopic(query)
                        .map { globalTopic -> globalTopic.toUiItem() }
                )
            }
        }
    }

    private fun Topic.toUiItem() = GlobalTopicUiState(
        topicId = id,
        name = UiText.Dynamic(name.formatted(locale))
    )

    data class UiState(
        val isFetching: Boolean = true,
        val topicsUiState: List<GlobalTopicUiState> = emptyList(),
        val isDeleting: Boolean = false
    )

    sealed interface Event {
        data object Fetch : Event
        data object BeginDelete : Event
        data object StopDelete : Event
        data class Search(val query: String) : Event
        data class TryDelete(val topicId: Int) : Event
    }

    sealed interface OnetimeEvent {
        data object FailedToDeleteDeadline : OnetimeEvent
    }

    @AssistedFactory
    interface Factory {
        fun create(locale: Locale): GlobalTopicListViewModel
    }
}

data class GlobalTopicUiState(
    val topicId: Int,
    val name: UiText
)