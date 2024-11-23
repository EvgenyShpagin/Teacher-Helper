package com.tusur.teacherhelper.presentation.topicperformance

import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.usecase.GetNextStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetPrevStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetStudentPerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetStudentUseCase
import com.tusur.teacherhelper.domain.usecase.SetStudentPerformanceUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AttendanceViewModel.Factory::class)
class AttendanceViewModel @AssistedInject constructor(
    @Assisted("currentStudentId") currentStudentId: Int,
    @Assisted allStudentIds: List<Int>,
    @Assisted("topicId") private val topicId: Int,
    @Assisted private val datetimeMillis: Long,
    private val getStudent: GetStudentUseCase,
    private val getStudentPerformance: GetStudentPerformanceUseCase,
    getNextStudent: GetNextStudentUseCase,
    getPrevStudent: GetPrevStudentUseCase,
    private val setStudentPerformance: SetStudentPerformanceUseCase,
) : StudentPerformanceBaseViewModel(
    currentStudentId,
    allStudentIds,
    getStudent,
    getNextStudent,
    getPrevStudent
) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun send(event: Event) {
        when (event) {
            Event.SetStudentAbsent -> setAbsent()
            Event.SetStudentExcused -> setExcused()
            Event.SetStudentPresent -> setPresent()
            Event.SetPrevStudent -> toPrevStudentAttendance()
            Event.SetNextStudent -> toNextStudentAttendance()
            Event.Fetch -> fetch()
        }
    }

    override suspend fun updateStudent(newStudent: Student?) {
        super.updateStudent(newStudent)
        val performance = getStudentPerformance(
            currentStudentId,
            topicId,
            datetimeMillis
        )
        val attendance = performance.attendance?.single()
        _uiState.update {
            it.copy(
                studentName = currentStudent!!.name.toString(),
                isPresent = attendance == PerformanceItem.Attendance.Present,
                isAbsent = attendance == PerformanceItem.Attendance.Absent,
                isExcused = attendance == PerformanceItem.Attendance.Excused,
                hasNextStudent = nextStudent != null,
                hasPrevStudent = prevStudent != null
            )
        }
    }

    private fun setExcused() {
        saveAttendance(PerformanceItem.Attendance.Excused)
        _uiState.update { it.copy(isExcused = true, isPresent = false, isAbsent = false) }
    }

    private fun setAbsent() {
        saveAttendance(PerformanceItem.Attendance.Absent)
        _uiState.update { it.copy(isExcused = false, isPresent = false, isAbsent = true) }
    }

    private fun setPresent() {
        saveAttendance(PerformanceItem.Attendance.Present)
        _uiState.update { it.copy(isExcused = false, isPresent = true, isAbsent = false) }
    }

    private fun saveAttendance(attendance: PerformanceItem.Attendance) {
        viewModelScope.launch {
            setStudentPerformance(
                currentStudentId,
                topicId,
                attendance,
                datetimeMillis
            )
        }
    }

    data class UiState(
        val studentName: String = "",
        val isPresent: Boolean = false,
        val isAbsent: Boolean = false,
        val isExcused: Boolean = false,
        val hasNextStudent: Boolean = false,
        val hasPrevStudent: Boolean = false
    )

    sealed interface Event {
        data object Fetch : Event
        data object SetNextStudent : Event
        data object SetPrevStudent : Event
        data object SetStudentAbsent : Event
        data object SetStudentExcused : Event
        data object SetStudentPresent : Event
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("currentStudentId") currentStudentId: Int,
            allStudentIds: List<Int>,
            @Assisted("topicId") topicId: Int,
            datetimeMillis: Long
        ): AttendanceViewModel
    }

}