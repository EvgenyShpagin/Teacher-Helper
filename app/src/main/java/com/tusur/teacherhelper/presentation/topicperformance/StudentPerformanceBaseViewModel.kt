package com.tusur.teacherhelper.presentation.topicperformance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.usecase.GetNextStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetPrevStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetStudentUseCase
import kotlinx.coroutines.launch

abstract class StudentPerformanceBaseViewModel(
    protected var currentStudentId: Int,
    var allStudentIds: List<Int>,
    private val getStudent: GetStudentUseCase,
    private val getNextStudent: GetNextStudentUseCase,
    private val getPrevStudent: GetPrevStudentUseCase
) : ViewModel() {

    protected var nextStudent: Student? = null
    protected var currentStudent: Student? = null
    protected var prevStudent: Student? = null

    protected open fun fetch() {
        viewModelScope.launch { updateStudent(currentStudent) }
    }

    protected fun toNextStudentAttendance() {
        viewModelScope.launch { updateStudent(newStudent = nextStudent) }
    }

    protected fun toPrevStudentAttendance() {
        viewModelScope.launch { updateStudent(newStudent = prevStudent) }
    }

    protected open suspend fun updateStudent(newStudent: Student?) {
        if (newStudent != null) {
            currentStudentId = newStudent.id
            currentStudent = newStudent
        } else {
            currentStudent = getStudent(currentStudentId)
        }
        nextStudent = getNextStudent(currentStudentId, allStudentIds)
        prevStudent = getPrevStudent(currentStudentId, allStudentIds)
    }
}