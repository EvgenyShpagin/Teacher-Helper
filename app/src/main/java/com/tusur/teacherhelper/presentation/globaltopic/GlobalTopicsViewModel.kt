package com.tusur.teacherhelper.presentation.globaltopic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.usecase.DeletePerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.DeleteTopicUseCase
import com.tusur.teacherhelper.domain.usecase.GetClassDatetimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetGlobalTopicsUseCase
import com.tusur.teacherhelper.domain.usecase.SetTopicDeadlineUseCase
import com.tusur.teacherhelper.domain.util.GLOBAL_TOPICS_SUBJECT_ID
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.presentation.App
import com.tusur.teacherhelper.presentation.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale


class GlobalTopicsViewModel(
    private val locale: Locale,
    private val getGlobalTopics: GetGlobalTopicsUseCase,
    private val deleteTopic: DeleteTopicUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()


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

    companion object {
        fun factory(locale: Locale) = viewModelFactory {
            initializer {
                GlobalTopicsViewModel(
                    locale = locale,
                    getGlobalTopics = GetGlobalTopicsUseCase(App.module.topicRepository),
                    deleteTopic = DeleteTopicUseCase(
                        App.module.topicRepository,
                        App.module.subjectGroupRepository,
                        SetTopicDeadlineUseCase(
                            App.module.topicRepository,
                            App.module.deadlineRepository
                        ),
                        DeletePerformanceUseCase(App.module.studentPerformanceRepository),
                        GetClassDatetimeUseCase(App.module.classDateRepository)
                    )
                )
            }
        }
    }
}

data class GlobalTopicUiState(
    val topicId: Int,
    val name: UiText
)