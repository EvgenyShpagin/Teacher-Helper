package com.tusur.teacherhelper.presentation.performance

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.usecase.GetGroupStudentsUseCase
import com.tusur.teacherhelper.domain.usecase.GetNextStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetPrevStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectStudentPerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectStudentSummaryAttendanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectStudentSummaryPerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectTopicsUseCase
import com.tusur.teacherhelper.domain.usecase.GetSuggestedProgressForGradeUseCase
import com.tusur.teacherhelper.domain.util.formattedShort
import com.tusur.teacherhelper.presentation.App
import com.tusur.teacherhelper.presentation.model.UiText
import com.tusur.teacherhelper.presentation.topicperformance.StudentPerformanceBaseViewModel
import com.tusur.teacherhelper.presentation.util.formatted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max


class StudentPerformanceViewModel(
    private val locale: Locale,
    private val subjectId: Int,
    initStudent: Int,
    private val groupId: Int,
    private val getGroupStudents: GetGroupStudentsUseCase,
    private val getSubjectTopics: GetSubjectTopicsUseCase,
    getStudent: GetStudentUseCase,
    private val getTotalStudentPerformanceProgress: GetSubjectStudentSummaryPerformanceUseCase,
    private val getTotalStudentAttendance: GetSubjectStudentSummaryAttendanceUseCase,
    getNextStudent: GetNextStudentUseCase,
    getPrevStudent: GetPrevStudentUseCase
) : StudentPerformanceBaseViewModel(
    currentStudentId = initStudent,
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
                topicTypesUiItems = topics.toTopicTypesUiItems()
            )
        }
    }

    private fun Topic.toUiItem() = TopicUiItem(
        id = id,
        name = UiText.Dynamic(name.formattedShort(locale)),
        isTakenInAccount = true,
        onClick = {
            viewModelScope.launch {
                _uiState.update { state ->
                    val takenInAccountItemCount = state.topicUiItems.count { it.isTakenInAccount }
                    val currentItemId = state.topicUiItems.find { it.id == id }
                    if (takenInAccountItemCount == 1 && currentItemId?.isTakenInAccount == true) {
                        return@update state
                    }
                    state.copy(
                        topicUiItems = state.topicUiItems.map {
                            if (it.id == id) {
                                it.copy(isTakenInAccount = !it.isTakenInAccount)
                            } else {
                                it
                            }
                        },
                        topicTypesUiItems = topics.toTopicTypesUiItems()
                    )
                }
            }
        }
    )

    private suspend fun List<Topic>.toTopicTypesUiItems(): List<TopicTypeUiItem> {
        val topicIds = map { it.id }
        val allTypesAttendance = getTotalStudentAttendance(currentStudentId, subjectId, topicIds)
        val allTypesProgress = getTotalStudentPerformanceProgress(
            studentId = currentStudentId,
            subjectId = subjectId,
            takenInAccountTopicIds = topicIds
        )
        val typeUiItems =
            ArrayList<TopicTypeUiItem>(allTypesAttendance.size + allTypesProgress.size)
        for (i in 0 until max(allTypesAttendance.size, allTypesProgress.size)) {
            val attendance = allTypesAttendance.getOrNull(i)
            val progress = if (attendance == null) {
                allTypesProgress.getOrNull(i)
            } else {
                allTypesProgress.find { it.first.id == attendance.first.id }
            }
            val typeId: Int
            val typeName: UiText
            var totalProgressText: UiText = UiText.empty
            var totalAttendanceText: UiText = UiText.empty
            if (attendance != null && progress != null) {
                typeId = attendance.first.id
                typeName = UiText.Dynamic(attendance.first.name)
                totalProgressText = progress.second.formatted()
                totalAttendanceText = attendance.second.formatted()
            } else if (attendance != null) {
                typeId = attendance.first.id
                typeName = UiText.Dynamic(attendance.first.name)
                totalAttendanceText = attendance.second.formatted()
            } else {
                typeId = progress!!.first.id
                typeName = UiText.Dynamic(progress.first.name)
                totalProgressText = progress.second.formatted()
            }
            typeUiItems.add(
                TopicTypeUiItem(
                    typeId,
                    typeName,
                    totalProgressText,
                    totalAttendanceText,
                    attendance != null,
                    progress != null
                )
            )
        }
        return typeUiItems
    }

    data class UiState(
        val studentName: UiText = UiText.empty,
        val hasPrevStudent: Boolean = false,
        val hasNextStudent: Boolean = false,
        val topicUiItems: List<TopicUiItem> = emptyList(),
        val topicTypesUiItems: List<TopicTypeUiItem> = emptyList()
    )

    sealed interface Event {
        data object Fetch : Event
        data object SetNextStudent : Event
        data object SetPrevStudent : Event
    }

    companion object {
        fun factory(locale: Locale, subjectId: Int, studentId: Int, groupId: Int) =
            viewModelFactory {
                initializer {
                    StudentPerformanceViewModel(
                        locale,
                        subjectId,
                        studentId,
                        groupId,
                        GetGroupStudentsUseCase(App.module.studentRepository),
                        GetSubjectTopicsUseCase(App.module.topicRepository),
                        GetStudentUseCase(App.module.studentRepository),
                        GetSubjectStudentSummaryPerformanceUseCase(
                            GetSubjectStudentPerformanceUseCase(
                                App.module.studentPerformanceRepository,
                                App.module.topicRepository
                            ),
                            GetSubjectTopicsUseCase(App.module.topicRepository),
                            GetSuggestedProgressForGradeUseCase()
                        ),
                        GetSubjectStudentSummaryAttendanceUseCase(
                            App.module.studentPerformanceRepository,
                            App.module.topicTypeRepository,
                            App.module.topicRepository
                        ),
                        GetNextStudentUseCase(App.module.studentRepository),
                        GetPrevStudentUseCase(App.module.studentRepository)
                    )
                }
            }
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
    val totalProgress: UiText,
    val totalAttendance: UiText,
    val hasAttendance: Boolean,
    val hasProgress: Boolean
)