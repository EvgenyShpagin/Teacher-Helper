package com.tusur.teacherhelper.presentation.topicperformance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.usecase.GetTopicGroupsPerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicNameByIdUseCase
import com.tusur.teacherhelper.domain.usecase.SearchInListUseCase
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.domain.util.shortName
import com.tusur.teacherhelper.presentation.core.model.UiText
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
import java.util.Locale


@HiltViewModel(assistedFactory = GroupsPerformanceViewModel.Factory::class)
class GroupsPerformanceViewModel @AssistedInject constructor(
    @Assisted private val locale: Locale,
    @Assisted private val topicId: Int,
    @Assisted private val performanceType: PerformanceType,
    @Assisted private val groupIdList: List<Int>,
    @Assisted private val datetimeMillis: Long,
    private val getGroupsPerformance: GetTopicGroupsPerformanceUseCase,
    private val getTopicName: GetTopicNameByIdUseCase,
    private val searchInList: SearchInListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val students = mutableListOf<Student>()

    private lateinit var groupPerformance: List<Pair<Group, List<Pair<Student, Performance>>>>

    fun fetch() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(topicName = getTopicName(topicId).formatted(locale))
            }
            getGroupsPerformance(
                topicId = topicId,
                groupIds = groupIdList,
                datetimeMillis = datetimeMillis
            ).collect { groupsWithStudentPerformance ->
                groupPerformance = groupsWithStudentPerformance
                _uiState.update {
                    it.copy(isFetching = false, groupItemsUiState = getItemsUiState())
                }
            }
        }
    }

    fun search(searchQuery: String) {
        _uiState.update { it.copy(groupItemsUiState = searchItemUiState(searchQuery)) }
    }

    fun stopSearch() {
        _uiState.update { it.copy(groupItemsUiState = getItemsUiState()) }
    }

    fun changeNameDisplayType() {
        _uiState.update { state ->
            state.copy(groupItemsUiState = state.groupItemsUiState.map { item ->
                when (item) {
                    is GroupItemUiState.Label -> item
                    is GroupItemUiState.Student -> item.copy(
                        studentName = getStudentName(
                            students.find { it.id == item.studentId }!!,
                            !state.showShortNames
                        )
                    )
                }
            }, showShortNames = !state.showShortNames)
        }
    }

    fun getAllStudentIds(): List<Int> {
        return uiState.value.groupItemsUiState
            .filterIsInstance<GroupItemUiState.Student>()
            .map { it.studentId }
    }

    private fun getItemsUiState(): List<GroupItemUiState> {
        return searchItemUiState(searchQuery = null)
    }

    private fun searchItemUiState(searchQuery: String?): List<GroupItemUiState> {
        var currentStudentOrdinal = 0
        return groupPerformance.map { (group, list) ->
            students.addAll(list.map { (student, _) -> student })

            val performanceOfSearchedStudents = searchQuery?.let {
                searchInList(searchQuery, list) { (student, _) -> student.name.full }
            } ?: list

            withLabelItem(
                groupNumber = group.number,
                itemsUiState = performanceOfSearchedStudents
                    .map { it.toUiItem(++currentStudentOrdinal) }
            )
        }.flatten()
    }

    private fun withLabelItem(
        groupNumber: String,
        itemsUiState: List<GroupItemUiState.Student>
    ): List<GroupItemUiState> {
        if (itemsUiState.isEmpty()) return emptyList()
        return listOf(GroupItemUiState.Label(groupNumber)) + itemsUiState
    }

    private fun getStudentName(student: Student, isShort: Boolean): UiText {
        return if (isShort) {
            UiText.Dynamic(student.shortName)
        } else {
            UiText.Dynamic(student.name.full)
        }
    }

    private fun Pair<Student, Performance>.toUiItem(ordinal: Int): GroupItemUiState.Student {
        return when (performanceType) {
            PerformanceType.ATTENDANCE -> toAttendanceUiItem(ordinal)
            PerformanceType.OTHER_PERFORMANCE -> toOtherPerformanceUiItem(ordinal)
        }
    }

    private fun Pair<Student, Performance>.toOtherPerformanceUiItem(ordinal: Int) =
        GroupItemUiState.Student(
            studentId = first.id,
            studentName = getStudentName(first, uiState.value.showShortNames),
            ordinal = ordinal,
            progress = second.progress?.toUiText(),
            grade = second.grade?.toUiText()
        )

    private fun Pair<Student, Performance>.toAttendanceUiItem(ordinal: Int) =
        GroupItemUiState.Student(
            studentId = first.id,
            studentName = getStudentName(first, uiState.value.showShortNames),
            ordinal = ordinal,
            attendanceIconRes = when (second.attendance?.single()) {
                PerformanceItem.Attendance.Absent -> R.drawable.ic_absent_24
                PerformanceItem.Attendance.Excused -> R.drawable.ic_minus_24
                PerformanceItem.Attendance.Present -> R.drawable.ic_add_24
                null -> null
            }
        )

    data class UiState(
        val isFetching: Boolean = true,
        val groupItemsUiState: List<GroupItemUiState> = emptyList(),
        val showShortNames: Boolean = true,
        val topicName: String = ""
    )

    @AssistedFactory
    interface Factory {
        fun create(
            locale: Locale,
            topicId: Int,
            performanceType: PerformanceType,
            groupIdList: List<Int>,
            datetimeMillis: Long
        ): GroupsPerformanceViewModel
    }
}

sealed interface GroupItemUiState {
    data class Student(
        val studentId: Int,
        val studentName: UiText,
        val ordinal: Int,
        val progress: UiText? = null,
        val grade: UiText? = null,
        val attendanceIconRes: Int? = null
    ) : GroupItemUiState

    data class Label(val groupNumber: String) : GroupItemUiState
}