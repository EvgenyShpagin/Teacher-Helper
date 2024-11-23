package com.tusur.teacherhelper.presentation.globaltopic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.error.DeleteTopicError.OthersDependOnTopicDeadline
import com.tusur.teacherhelper.domain.usecase.DeleteTopicUseCase
import com.tusur.teacherhelper.domain.usecase.GetGlobalTopicsUseCase
import com.tusur.teacherhelper.domain.util.GLOBAL_TOPICS_SUBJECT_ID
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.globaltopic.GlobalTopicsViewModel.OnetimeEvent.FailedToDeleteDeadline
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


@HiltViewModel(assistedFactory = GlobalTopicsViewModel.Factory::class)
class GlobalTopicsViewModel @AssistedInject constructor(
    @Assisted private val locale: Locale,
    private val getGlobalTopics: GetGlobalTopicsUseCase,
    private val deleteTopic: DeleteTopicUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _onetimeEvents = Channel<OnetimeEvent>()
    val onetimeEvent = _onetimeEvents.receiveAsFlow()


    fun fetch() {
        viewModelScope.launch {
            getGlobalTopics().collect { globalTopics ->
                _uiState.update { state ->
                    state.copy(globalTopicItemsUiState = globalTopics.map { it.toUiItem() })
                }
            }
        }
    }

    fun deleteTopic(topicId: Int) {
        viewModelScope.launch {
            deleteTopic(topicId, GLOBAL_TOPICS_SUBJECT_ID)
                .onFailure { error ->
                    when (error) {
                        OthersDependOnTopicDeadline -> {
                            _onetimeEvents.send(FailedToDeleteDeadline)
                        }
                    }
                }
            stopDelete()
        }
    }

    fun startDelete() {
        _uiState.update {
            it.copy(isDeleting = true)
        }
    }

    fun stopDelete() {
        _uiState.update {
            it.copy(isDeleting = false)
        }
    }

    private fun Topic.toUiItem() = GlobalTopicUiState(
        topicId = id, name = UiText.Dynamic(name.formatted(locale))
    )

    data class UiState(
        val globalTopicItemsUiState: List<GlobalTopicUiState> = emptyList(),
        val isDeleting: Boolean = false
    )

    sealed interface OnetimeEvent {
        data object FailedToDeleteDeadline : OnetimeEvent
    }

    @AssistedFactory
    interface Factory {
        fun create(locale: Locale): GlobalTopicsViewModel
    }
}

data class GlobalTopicUiState(
    val topicId: Int,
    val name: UiText
)