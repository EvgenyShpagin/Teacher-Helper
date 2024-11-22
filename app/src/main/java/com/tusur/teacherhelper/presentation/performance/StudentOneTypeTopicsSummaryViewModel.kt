package com.tusur.teacherhelper.presentation.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.usecase.GetStudentOneTypeTopicsPerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectStudentPerformanceUseCase
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.domain.util.getTotalAttendance
import com.tusur.teacherhelper.presentation.core.App
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.core.util.formatted
import com.tusur.teacherhelper.presentation.core.util.toUiText
import com.tusur.teacherhelper.presentation.topic.PerformanceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale


class StudentOneTypeTopicsResultsViewModel(
    private val locale: Locale,
    private val performanceType: PerformanceType,
    private val subjectId: Int,
    private val studentId: Int,
    private val topicTypeId: Int,
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
            topicName = UiText.Dynamic(topic.name.formatted(locale)),
            results = if (performanceType == PerformanceType.OTHER_PERFORMANCE) {
                performance.grade?.toUiText() ?: performance.progress!!.toUiText()
            } else {
                performance.attendance!!.getTotalAttendance().formatted()
            }
        )
    }

    data class UiState(
        val title: UiText = UiText.empty,
        val topicUiItems: List<TopicResultUiItem> = emptyList()
    )

    companion object {
        fun factory(
            locale: Locale,
            performanceType: PerformanceType,
            subjectId: Int,
            studentId: Int,
            topicTypeId: Int
        ) = object : ViewModelProvider.Factory {
            private val getTotalStudentPerformance = GetSubjectStudentPerformanceUseCase(
                App.module.studentPerformanceRepository,
                App.module.topicRepository
            )
            private val getStudentOneTypeTopicsProgress =
                GetStudentOneTypeTopicsPerformanceUseCase(getTotalStudentPerformance)

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StudentOneTypeTopicsResultsViewModel(
                    locale = locale,
                    performanceType = performanceType,
                    studentId = studentId,
                    subjectId = subjectId,
                    topicTypeId = topicTypeId,
                    getStudentOneTypeTopicsPerformance = getStudentOneTypeTopicsProgress
                ) as T
            }
        }
    }
}

data class TopicResultUiItem(
    val topicId: Int,
    val topicName: UiText = UiText.empty,
    val results: UiText = UiText.empty
)