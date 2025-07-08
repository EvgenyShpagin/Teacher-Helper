package com.tusur.teacherhelper.presentation.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.usecase.GetStudentOneTypeTopicsPerformanceUseCase
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.domain.util.getTotalAttendance
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.core.util.formatted
import com.tusur.teacherhelper.presentation.core.util.toUiText
import com.tusur.teacherhelper.presentation.topic.PerformanceType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel(assistedFactory = StudentOneTypeTopicsResultsViewModel.Factory::class)
class StudentOneTypeTopicsResultsViewModel @AssistedInject constructor(
    @Assisted private val performanceType: PerformanceType,
    @Assisted("subjectId") private val subjectId: Int,
    @Assisted("studentId") private val studentId: Int,
    @Assisted("topicTypeId") private val topicTypeId: Int,
    private val getStudentOneTypeTopicsPerformance: GetStudentOneTypeTopicsPerformanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()


    fun fetch() {
        viewModelScope.launch {
            _uiState.update {
                UiState(
                    title = if (performanceType == PerformanceType.OTHER_PERFORMANCE) {
                        UiText.Resource(R.string.dialog_performance_title)
                    } else {
                        UiText.Resource(R.string.dialog_attendance_title)
                    },
                    topicUiItems = getStudentOneTypeTopicsPerformance(
                        studentId,
                        subjectId,
                        topicTypeId
                    )
                        .map { it.toUiItem() }
                )
            }
        }
    }

    private fun Pair<Topic, Performance>.toUiItem(): TopicResultUiItem {
        val (topic, performance) = this
        return TopicResultUiItem(
            topicId = topic.id,
            topicName = UiText.Dynamic(topic.name.formatted()),
            results = if (performanceType == PerformanceType.OTHER_PERFORMANCE) {
                performance.grade?.toUiText()
                    ?: performance.progress?.toUiText()
                    ?: performance.assessment!!.toUiText()
            } else {
                performance.attendance!!.getTotalAttendance().formatted()
            }
        )
    }

    data class UiState(
        val title: UiText = UiText.empty,
        val topicUiItems: List<TopicResultUiItem> = emptyList()
    )

    @AssistedFactory
    interface Factory {
        fun create(
            performanceType: PerformanceType,
            @Assisted("subjectId") subjectId: Int,
            @Assisted("studentId") studentId: Int,
            @Assisted("topicTypeId") topicTypeId: Int
        ): StudentOneTypeTopicsResultsViewModel
    }
}

data class TopicResultUiItem(
    val topicId: Int,
    val topicName: UiText = UiText.empty,
    val results: UiText = UiText.empty
)