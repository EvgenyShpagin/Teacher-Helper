package com.tusur.teacherhelper.presentation.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.error.DeleteTopicError.OthersDependOnTopicDeadline
import com.tusur.teacherhelper.domain.usecase.CancelTopicUseCase
import com.tusur.teacherhelper.domain.usecase.DeleteTopicUseCase
import com.tusur.teacherhelper.domain.usecase.DoesTopicHaveClassDatetimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetDeadlineUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicAsFlowUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicNameByIdUseCase
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.domain.util.formattedShort
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.topic.TopicViewModel.OnetimeEvent.FailedToDeleteDeadline
import com.tusur.teacherhelper.presentation.topic.TopicViewModel.OnetimeEvent.NavigateBack
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


@HiltViewModel(assistedFactory = TopicViewModel.Factory::class)
class TopicViewModel @AssistedInject constructor(
    @Assisted("subjectId") private val subjectId: Int,
    @Assisted("topicId") private val topicId: Int,
    @Assisted private val isJustCreated: Boolean,
    @Assisted private val locale: Locale,
    private val getTopicAsFlow: GetTopicAsFlowUseCase,
    private val getTopicName: GetTopicNameByIdUseCase,
    private val cancelTopic: CancelTopicUseCase,
    private val deleteTopic: DeleteTopicUseCase,
    private val getDeadline: GetDeadlineUseCase,
    private val doesTopicHaveClassDatetime: DoesTopicHaveClassDatetimeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _onetimeEvents = Channel<OnetimeEvent>()
    val onetimeEvent = _onetimeEvents.receiveAsFlow()

    private lateinit var topic: Topic


    fun fetch() {
        viewModelScope.launch {
            getTopicAsFlow(topicId).collect { topic ->
                this@TopicViewModel.topic = topic
                val deadlineText = if (topic.deadline?.owningTopicId != null) {
                    if (topic.deadline.owningTopicId == topicId) {
                        UiText.Dynamic(getDeadline(topicId)!!.date.formatted(locale))
                    } else {
                        UiText.Resource(
                            R.string.topic_associated_deadline_label,
                            getTopicName(topic.deadline.owningTopicId).formattedShort(locale)
                        )
                    }
                } else {
                    UiText.empty
                }
                _uiState.update {
                    it.copy(
                        isJustCreated = isJustCreated,
                        isTopicCancelled = topic.isCancelled,
                        supportsGrades = topic.type.isGradeAcceptable,
                        supportsAssessment = topic.type.isAssessmentAcceptable,
                        supportsDeadline = topic.type.canDeadlineBeSpecified,
                        supportsProgress = topic.type.isProgressAcceptable,
                        supportsAttendance = topic.type.isAttendanceAcceptable,
                        hasClassDays = doesTopicHaveClassDatetime(topicId),
                        topicName = topic.name.formatted(locale),
                        deadlineText = deadlineText
                    )
                }
            }
        }
    }

    fun cancelTopic() {
        viewModelScope.launch {
            cancelTopic(topic)
            _onetimeEvents.send(NavigateBack)
        }
    }

    fun deleteTopic() {
        viewModelScope.launch {
            deleteTopic(topicId = topicId, subjectId = subjectId)
                .onFailure { error ->
                    when (error) {
                        OthersDependOnTopicDeadline -> _onetimeEvents.send(FailedToDeleteDeadline)
                    }
                }.onSuccess {
                    _onetimeEvents.send(NavigateBack)
                }
        }
    }

    data class UiState(
        val isJustCreated: Boolean = false,
        val isTopicCancelled: Boolean = false,
        val supportsGrades: Boolean = false,
        val supportsProgress: Boolean = false,
        val supportsAssessment: Boolean = false,
        val supportsDeadline: Boolean = false,
        val supportsAttendance: Boolean = false,
        val deadlineText: UiText = UiText.empty,
        val hasClassDays: Boolean = false,
        val topicName: String = ""
    )

    sealed interface OnetimeEvent {
        data object FailedToDeleteDeadline : OnetimeEvent
        data object NavigateBack : OnetimeEvent
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("subjectId") subjectId: Int,
            @Assisted("topicId") topicId: Int,
            isJustCreated: Boolean,
            locale: Locale
        ): TopicViewModel
    }
}