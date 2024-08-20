package com.tusur.teacherhelper.presentation.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.usecase.CancelTopicUseCase
import com.tusur.teacherhelper.domain.usecase.DeletePerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.DeleteTopicDeadlineUseCase
import com.tusur.teacherhelper.domain.usecase.DeleteTopicUseCase
import com.tusur.teacherhelper.domain.usecase.DoesTopicHaveClassDatetimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetClassDatetimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetDeadlineUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicAsFlowUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicNameByIdUseCase
import com.tusur.teacherhelper.domain.usecase.UpdateSubjectTopicUseCase
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.domain.util.formattedShort
import com.tusur.teacherhelper.presentation.App
import com.tusur.teacherhelper.presentation.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale


class TopicViewModel(
    private val subjectId: Int,
    private val topicId: Int,
    private val isJustCreated: Boolean,
    private val locale: Locale,
    private val getTopicAsFlow: GetTopicAsFlowUseCase,
    private val getTopicName: GetTopicNameByIdUseCase,
    private val cancelTopic: CancelTopicUseCase,
    private val deleteTopic: DeleteTopicUseCase,
    private val getDeadline: GetDeadlineUseCase,
    private val doesTopicHaveClassDatetime: DoesTopicHaveClassDatetimeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

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
        viewModelScope.launch { cancelTopic(topic) }
    }

    fun deleteTopic() {
        viewModelScope.launch { deleteTopic(topicId = topicId, subjectId = subjectId) }
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

    companion object {

        fun factory(
            subjectId: Int,
            topicId: Int,
            isJustCreated: Boolean,
            locale: Locale
        ) = viewModelFactory {
            initializer {
                TopicViewModel(
                    subjectId = subjectId,
                    topicId = topicId,
                    isJustCreated = isJustCreated,
                    locale = locale,
                    getTopicAsFlow = GetTopicAsFlowUseCase(App.module.topicRepository),
                    getTopicName = GetTopicNameByIdUseCase(App.module.topicRepository),
                    cancelTopic = CancelTopicUseCase(
                        UpdateSubjectTopicUseCase(
                            App.module.topicRepository,
                            App.module.subjectRepository,
                            App.module.deadlineRepository
                        )
                    ),
                    deleteTopic = DeleteTopicUseCase(
                        App.module.topicRepository,
                        App.module.subjectGroupRepository,
                        DeleteTopicDeadlineUseCase(
                            App.module.topicRepository,
                            App.module.deadlineRepository
                        ),
                        DeletePerformanceUseCase(App.module.studentPerformanceRepository),
                        GetClassDatetimeUseCase(App.module.classDateRepository)
                    ),
                    getDeadline = GetDeadlineUseCase(App.module.deadlineRepository),
                    doesTopicHaveClassDatetime = DoesTopicHaveClassDatetimeUseCase(
                        GetClassDatetimeUseCase(App.module.classDateRepository)
                    )
                )
            }
        }

    }
}