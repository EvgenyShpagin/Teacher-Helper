package com.tusur.teacherhelper.presentation.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Deadline
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.error.DeadlineUpdateError
import com.tusur.teacherhelper.domain.usecase.GetAllTopicsDeadlineUseCase
import com.tusur.teacherhelper.domain.usecase.GetDeadlineUseCase
import com.tusur.teacherhelper.domain.usecase.SetTopicDeadlineUseCase
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.presentation.core.model.UiText
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
import kotlinx.datetime.LocalDate
import java.util.Locale


@HiltViewModel(assistedFactory = DeadlineViewModel.Factory::class)
class DeadlineViewModel @AssistedInject constructor(
    @Assisted private val locale: Locale,
    @Assisted private val topicId: Int,
    private val getTopicDeadline: GetDeadlineUseCase,
    private val setTopicDeadline: SetTopicDeadlineUseCase,
    private val getAllTopicsDeadline: GetAllTopicsDeadlineUseCase
) : ViewModel() {
    private var deadline: Deadline? = null

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _onetimeEvents = Channel<OnetimeEvent>()
    val onetimeEvent = _onetimeEvents.receiveAsFlow()

    fun fetch() {
        viewModelScope.launch {
            deadline = getTopicDeadline(topicId)
            val allTopicDeadlines = getAllTopicsDeadline().filter { it.first.id != topicId }
            _uiState.update {
                it.copy(
                    isDeadlineSet = deadline != null,
                    deadlineItems = allTopicDeadlines.toUiItems(),
                    anyDeadlineExists = allTopicDeadlines.isNotEmpty()
                )
            }
        }
    }

    fun removeDeadline() {
        setDeadline(null)
    }

    fun setDeadline(date: LocalDate) {
        setDeadline(Deadline(NO_ID, date, topicId))
    }

    data class UiState(
        val isDeadlineSet: Boolean = true,
        val deadlineItems: List<DeadlineUiItem> = emptyList(),
        val anyDeadlineExists: Boolean = true
    )

    data class DeadlineUiItem(
        val text: UiText,
        val isSelected: Boolean,
        val select: () -> Unit
    )

    sealed interface OnetimeEvent {
        data object FailedToDeleteDeadline : OnetimeEvent
    }

    private fun List<Pair<Topic, Deadline>>.toUiItems(): List<DeadlineUiItem> {
        val selectedIndex = indexOfFirst { it.first.id == deadline?.owningTopicId }
            .coerceAtLeast(0)
        val noDeadlineItem = DeadlineUiItem(
            text = UiText.Resource(R.string.deadline_not_set),
            isSelected = selectedIndex == 0,
            select = { deadline?.owningTopicId?.let { removeDeadline() } }
        )
        return listOf(noDeadlineItem) + mapIndexed { index, (topic, deadline) ->
            DeadlineUiItem(
                text = UiText.Dynamic(topic.name.formatted()),
                isSelected = selectedIndex == index,
                select = { setDeadline(deadline) }
            )
        }
    }

    private fun setDeadline(deadline: Deadline?) {
        viewModelScope.launch {
            setTopicDeadline(topicId, deadline).onFailure { error ->
                when (error) {
                    DeadlineUpdateError.OtherTopicsDependOn -> {
                        _onetimeEvents.send(OnetimeEvent.FailedToDeleteDeadline)
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(locale: Locale, topicId: Int): DeadlineViewModel
    }
}