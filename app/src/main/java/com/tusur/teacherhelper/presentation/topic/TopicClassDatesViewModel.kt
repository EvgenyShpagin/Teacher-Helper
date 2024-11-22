package com.tusur.teacherhelper.presentation.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Datetime
import com.tusur.teacherhelper.domain.usecase.EditTopicClassDayUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicClassDaysUseCase
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.presentation.core.App
import com.tusur.teacherhelper.presentation.core.model.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale


class TopicClassDatesViewModel(
    private val locale: Locale,
    private val topicId: Int,
    private val getTopicClassDays: GetTopicClassDaysUseCase,
    private val editTopicClassDay: EditTopicClassDayUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val onetimeEventChannel = Channel<Event>()
    val onetimeEvent = onetimeEventChannel.receiveAsFlow()

    fun fetch() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(classDays = getTopicClassDays(topicId).map { it.toUiItem() })
            }
        }
    }

    fun editClassDay(oldDatetimeMs: Long, newDatetimeMs: Long) {
        if (oldDatetimeMs == newDatetimeMs) return
        viewModelScope.launch {
            if (uiState.value.classDays.any { it.datetimeMillis == newDatetimeMs }) {
                val updateErrorText = UiText.Resource(R.string.class_day_edit_error_message)
                onetimeEventChannel.send(Event.UpdateError(updateErrorText))
                return@launch
            }
            editTopicClassDay(topicId, oldDatetimeMs, newDatetimeMs)
            onetimeEventChannel.send(Event.DatetimeUpdated)
        }
    }

    private fun Datetime.toUiItem() = DatetimeItemUiState(
        toMillis(),
        UiText.Dynamic(formatted(locale))
    )

    data class UiState(val classDays: List<DatetimeItemUiState> = emptyList())

    sealed interface Event {
        data object DatetimeUpdated : Event
        data class UpdateError(val reason: UiText) : Event
    }

    companion object {
        fun factory(locale: Locale, topicId: Int) = viewModelFactory {
            initializer {
                TopicClassDatesViewModel(
                    locale,
                    topicId,
                    GetTopicClassDaysUseCase(App.module.classDateRepository),
                    EditTopicClassDayUseCase(App.module.classDateRepository)
                )
            }
        }
    }
}

data class DatetimeItemUiState(
    val datetimeMillis: Long,
    val datetimeText: UiText
)