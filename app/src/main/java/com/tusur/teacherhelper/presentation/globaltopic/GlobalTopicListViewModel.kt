package com.tusur.teacherhelper.presentation.globaltopic

import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.error.DeleteTopicError.OthersDependOnTopicDeadline
import com.tusur.teacherhelper.domain.usecase.DeleteTopicUseCase
import com.tusur.teacherhelper.domain.usecase.GetGlobalTopicsUseCase
import com.tusur.teacherhelper.domain.usecase.SearchGlobalTopicUseCase
import com.tusur.teacherhelper.domain.util.GLOBAL_TOPICS_SUBJECT_ID
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.globaltopic.GlobalTopicListEffect.FailedToDeleteDeadline
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@HiltViewModel(assistedFactory = GlobalTopicListViewModel.Factory::class)
class GlobalTopicListViewModel @AssistedInject constructor(
    @Assisted private val locale: Locale,
    private val getGlobalTopics: GetGlobalTopicsUseCase,
    private val searchGlobalTopic: SearchGlobalTopicUseCase,
    private val deleteTopic: DeleteTopicUseCase
) : TopLevelListViewModel<GlobalTopicListUiState, GlobalTopicListEffect>(
    initialUiState = GlobalTopicListUiState()
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
            getGlobalTopics().collect { globalTopics ->
                val uiGlobalTopics = globalTopics.map { it.toUiItem() }
                updateState { state ->
                    state.copy(
                        isFetching = false,
                        allItems = uiGlobalTopics,
                        searchedItems = uiGlobalTopics
                    )
                }
            }
        }
    }

    private fun delete(topicId: Int) {
        viewModelScope.launch {
            deleteTopic(
                topicId = topicId,
                subjectId = GLOBAL_TOPICS_SUBJECT_ID
            ).onFailure { error ->
                when (error) {
                    OthersDependOnTopicDeadline -> triggerEffect(FailedToDeleteDeadline)
                }
            }
        }
    }

    private fun search(query: String) {
        updateState { it.copy(isFetching = true) }
        viewModelScope.launch {
            val globalTopics = searchGlobalTopic("%$query%")
                .map { globalTopic -> globalTopic.toUiItem() }
            updateState {
                it.copy(isFetching = false, searchedItems = globalTopics)
            }
        }
    }

    private fun Topic.toUiItem() = GlobalTopicUiState(
        topicId = id,
        name = UiText.Dynamic(name.formatted(locale))
    )


    @AssistedFactory
    interface Factory {
        fun create(locale: Locale): GlobalTopicListViewModel
    }
}