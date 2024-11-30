package com.tusur.teacherhelper.presentation.performance

import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.usecase.GetGroupStudentsUseCase
import com.tusur.teacherhelper.domain.usecase.GetNextStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetPrevStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectStudentSummaryAttendanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectStudentSummaryPerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectTopicsUseCase
import com.tusur.teacherhelper.domain.util.formattedShort
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.core.util.formatted
import com.tusur.teacherhelper.presentation.topic.PerformanceType
import com.tusur.teacherhelper.presentation.topicperformance.StudentPerformanceBaseViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale


@HiltViewModel(assistedFactory = StudentPerformanceViewModel.Factory::class)
class StudentPerformanceViewModel @AssistedInject constructor(
    @Assisted private val locale: Locale,
    @Assisted("subjectId") private val subjectId: Int,
    @Assisted("initStudentId") initStudentId: Int,
    @Assisted("groupId") private val groupId: Int,
    private val getGroupStudents: GetGroupStudentsUseCase,
    private val getSubjectTopics: GetSubjectTopicsUseCase,
    getStudent: GetStudentUseCase,
    private val getTotalStudentPerformanceProgress: GetSubjectStudentSummaryPerformanceUseCase,
    private val getTotalStudentAttendance: GetSubjectStudentSummaryAttendanceUseCase,
    getNextStudent: GetNextStudentUseCase,
    getPrevStudent: GetPrevStudentUseCase
) : StudentPerformanceBaseViewModel(
    currentStudentId = initStudentId,
    allStudentIds = emptyList(),
    getStudent = getStudent,
    getNextStudent = getNextStudent,
    getPrevStudent = getPrevStudent
) {
    private var topics: List<Topic> = emptyList()

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun send(event: Event) {
        when (event) {
            Event.Fetch -> fetch()
            Event.SetNextStudent -> toNextStudentAttendance()
            Event.SetPrevStudent -> toPrevStudentAttendance()
        }
    }

    override fun fetch() {
        viewModelScope.launch {
            allStudentIds = getGroupStudents(groupId).first().map { it.id }
            topics = getSubjectTopics(subjectId = subjectId, withCancelled = false).first()
            super.fetch()
        }
    }

    override suspend fun updateStudent(newStudent: Student?) {
        super.updateStudent(newStudent)
        _uiState.update { state ->
            state.copy(
                studentName = UiText.Dynamic(currentStudent!!.name.toString()),
                hasPrevStudent = prevStudent != null,
                hasNextStudent = nextStudent != null,
                topicUiItems = topics.map { it.toUiItem() },
                performanceTopicTypeUiItems =
                    topics.toTopicTypeUiItems(PerformanceType.OTHER_PERFORMANCE),
                attendanceTopicTypeUiItems =
                    topics.toTopicTypeUiItems(PerformanceType.ATTENDANCE)
            )
        }
    }

    private fun Topic.toUiItem() = TopicUiItem(
        id = id,
        name = UiText.Dynamic(name.formattedShort(locale)),
        isTakenInAccount = true,
        onClick = {
            onTopicAccountingChange(this)
        }
    )

    private fun onTopicAccountingChange(topic: Topic) {
        viewModelScope.launch {
            _uiState.update { state ->
                val takenInAccountItemCount = state.topicUiItems.count { it.isTakenInAccount }
                val currentItemId = state.topicUiItems.find { it.id == topic.id }
                if (takenInAccountItemCount == 1 && currentItemId?.isTakenInAccount == true) {
                    return@update state
                }
                val updatedTopicUiItems = state.topicUiItems.map {
                    if (it.id == topic.id) {
                        it.copy(isTakenInAccount = !it.isTakenInAccount)
                    } else {
                        it
                    }
                }

                val takenIntoAccountTopicIds = updatedTopicUiItems
                    .filter { it.isTakenInAccount }
                    .map { it.id }

                val takenIntoAccountTopics = topics
                    .filter { it.id in takenIntoAccountTopicIds }

                state.copy(
                    topicUiItems = updatedTopicUiItems,
                    performanceTopicTypeUiItems = takenIntoAccountTopics
                        .toTopicTypeUiItems(PerformanceType.OTHER_PERFORMANCE),
                    attendanceTopicTypeUiItems = takenIntoAccountTopics
                        .toTopicTypeUiItems(PerformanceType.ATTENDANCE)
                )
            }
        }
    }

    private suspend fun List<Topic>.toTopicTypeUiItems(
        performanceType: PerformanceType
    ): List<TopicTypeUiItem> {
        val accountingIds = map { topic -> topic.id }

        val typeToProgressList = when (performanceType) {
            PerformanceType.ATTENDANCE -> getTotalStudentAttendance(
                studentId = currentStudent!!.id,
                subjectId = subjectId,
                takenInAccountTopicIds = accountingIds
            )

            PerformanceType.OTHER_PERFORMANCE -> getTotalStudentPerformanceProgress(
                studentId = currentStudent!!.id,
                subjectId = subjectId,
                takenInAccountTopicIds = accountingIds
            )
        }

        return typeToProgressList.map { (type, progress) ->
            TopicTypeUiItem(
                typeId = type.id,
                name = UiText.Dynamic(type.name),
                progress = progress.formatted(),
            )
        }
    }

    data class UiState(
        val studentName: UiText = UiText.empty,
        val hasPrevStudent: Boolean = false,
        val hasNextStudent: Boolean = false,
        val topicUiItems: List<TopicUiItem> = emptyList(),
        val performanceTopicTypeUiItems: List<TopicTypeUiItem> = emptyList(),
        val attendanceTopicTypeUiItems: List<TopicTypeUiItem> = emptyList(),
    )

    sealed interface Event {
        data object Fetch : Event
        data object SetNextStudent : Event
        data object SetPrevStudent : Event
    }

    @AssistedFactory
    interface Factory {
        fun create(
            locale: Locale,
            @Assisted("subjectId") subjectId: Int,
            @Assisted("initStudentId") initStudentId: Int,
            @Assisted("groupId") groupId: Int
        ): StudentPerformanceViewModel
    }
}

data class TopicUiItem(
    val id: Int,
    val name: UiText,
    val isTakenInAccount: Boolean,
    val onClick: () -> Unit
)

data class TopicTypeUiItem(
    val typeId: Int,
    val name: UiText,
    val progress: UiText
)