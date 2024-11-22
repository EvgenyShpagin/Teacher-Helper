package com.tusur.teacherhelper.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.error.StudentNameError
import com.tusur.teacherhelper.domain.usecase.AddStudentUseCase
import com.tusur.teacherhelper.domain.usecase.ChangeStudentNameUseCase
import com.tusur.teacherhelper.domain.usecase.CheckStudentNameUseCase
import com.tusur.teacherhelper.domain.usecase.DeleteStudentUseCase
import com.tusur.teacherhelper.domain.usecase.FormatStudentNameUseCase
import com.tusur.teacherhelper.domain.usecase.GetGroupByIdUseCase
import com.tusur.teacherhelper.domain.usecase.GetGroupStudentsUseCase
import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.presentation.core.App
import com.tusur.teacherhelper.presentation.core.model.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class GroupStudentsViewModel(
    private val groupId: Int,
    private val getGroupName: GetGroupByIdUseCase,
    private val getGroupStudents: GetGroupStudentsUseCase,
    private val checkStudentName: CheckStudentNameUseCase,
    private val formatStudentName: FormatStudentNameUseCase,
    private val addStudent: AddStudentUseCase,
    private val deleteStudent: DeleteStudentUseCase,
    private val changeStudentName: ChangeStudentNameUseCase
) : ViewModel() {

    private var originStudentList = emptyList<Student>()

    private val tempStudent = StudentItemUiState(
        id = TEMP_STUDENT_ID,
        name = "",
        isEditing = true,
        errorMessage = null,
        onEdit = { setEditable(TEMP_STUDENT_ID) },
        onSave = { newName -> updateStudentName(null, newName) }
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(groupNumber = getGroupName(groupId).number)
            }
            getGroupStudents(groupId).flowOn(Dispatchers.IO).collect { students ->
                originStudentList = students
                _uiState.update { state ->
                    state.copy(studentItemsUiState = students.map { it.toUiItem() })
                }
            }

        }
    }

    fun addStudentWithoutName() {
        _uiState.update { it.copy(studentItemsUiState = it.studentItemsUiState + tempStudent) }
    }

    fun deleteStudent(studentId: Int) {
        viewModelScope.launch {
            deleteStudent.invoke(studentId)
        }
    }

    fun stopEditing() {
        val edited = uiState.value.studentItemsUiState.find { it.isEditing } ?: return
        edited.onEdit()
    }

    fun startDelete() {
        _uiState.update { it.copy(isDeleting = true) }
    }

    fun stopDelete() {
        _uiState.update { it.copy(isDeleting = false) }
    }

    private suspend fun addNewStudent(name: Student.Name) {
        when (val checkResult = checkStudentName(TEMP_STUDENT_ID, name)) {
            is Result.Error -> if (checkResult.error == StudentNameError.INCORRECT) {
                setError(TEMP_STUDENT_ID)
                delayForRemoveError(TEMP_STUDENT_ID)
            }

            is Result.Success -> viewModelScope.launch { addStudent(name, groupId) }
        }
    }

    private fun updateStudentName(student: Student?, name: String) {
        viewModelScope.launch {
            val formattedName = formatStudentName(name)
            if (student == null) {
                addNewStudent(formattedName)
                return@launch
            }
            when (val checkResult = checkStudentName(TEMP_STUDENT_ID, formattedName)) {
                is Result.Success -> changeStudentName.invoke(student, groupId, formattedName)
                is Result.Error -> {
                    if (checkResult.error == StudentNameError.NOT_CHANGED) {
                        _uiState.update { state ->
                            state.copy(
                                studentItemsUiState = state.studentItemsUiState
                                    .map {
                                        if (it.id == student.id) {
                                            it.copy(isEditing = false)
                                        } else {
                                            it
                                        }
                                    }
                            )
                        }
                    } else {
                        setError(student.id)
                        delayForRemoveError(student.id)
                    }
                }
            }
        }
    }

    private fun setError(errorStudentId: Int) {
        _uiState.update { state ->
            state.copy(studentItemsUiState = state.studentItemsUiState.map {
                if (it.id == errorStudentId) {
                    it.copy(errorMessage = UiText.Resource(R.string.incorrect_student_name_error))
                } else {
                    it
                }
            })
        }
    }

    private fun delayForRemoveError(errorStudentId: Int) {
        viewModelScope.launch {
            delay(2000)
            _uiState.update { state ->
                state.copy(studentItemsUiState = state.studentItemsUiState.map {
                    if (it.id == errorStudentId && it.errorMessage != null) {
                        it.copy(errorMessage = null)
                    } else {
                        it
                    }
                })
            }
        }
    }

    private fun setEditable(id: Int) {
        var notSavedStudent: StudentItemUiState? = null
        val newList = uiState.value.studentItemsUiState.map { student ->
            when {
                // clicked item
                student.id == id -> student.also {
                    if (it.isEditing && isStudentNotSaved(it)) {
                        notSavedStudent = it
                    }
                }.copy(isEditing = !student.isEditing)
                // previously clicked item
                student.isEditing -> student.copy(isEditing = false).also {
                    if (isStudentNotSaved(it)) {
                        notSavedStudent = it
                    }
                }
                // default item
                else -> student
            }
        }.filter { it.id != notSavedStudent?.id }

        _uiState.update { uiState ->
            uiState.copy(studentItemsUiState = newList)
        }
    }

    private fun isStudentNotSaved(student: StudentItemUiState): Boolean {
        return student.id == TEMP_STUDENT_ID
    }

    data class UiState(
        val groupNumber: String = "",
        val studentItemsUiState: List<StudentItemUiState> = emptyList(),
        val isDeleting: Boolean = false
    ) {
        val isEditing: Boolean
            get() = studentItemsUiState.any { it.isEditing }
    }

    data class StudentItemUiState(
        val id: Int,
        val name: String,
        val isEditing: Boolean,
        val errorMessage: UiText?,
        val onEdit: () -> Unit,
        val onSave: (newName: String) -> Unit
    )

    private fun Student.toUiItem() = StudentItemUiState(
        id = id,
        name = name.toString(),
        isEditing = false,
        errorMessage = null,
        onEdit = { setEditable(id) },
        onSave = { newName -> updateStudentName(this, newName) }
    )


    companion object {

        private const val TEMP_STUDENT_ID = -1

        fun factory(groupId: Int) = object : ViewModelProvider.Factory {

            private val getGroupName = GetGroupByIdUseCase(App.module.groupRepository)
            private val getGroupStudents = GetGroupStudentsUseCase(App.module.studentRepository)
            private val checkStudentName = CheckStudentNameUseCase(App.module.studentRepository)
            private val formatStudentName = FormatStudentNameUseCase()
            private val addStudent = AddStudentUseCase(App.module.studentRepository)
            private val deleteStudent = DeleteStudentUseCase(App.module.studentRepository)
            private val changeStudentName = ChangeStudentNameUseCase(App.module.studentRepository)

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GroupStudentsViewModel(
                    groupId,
                    getGroupName,
                    getGroupStudents,
                    checkStudentName,
                    formatStudentName,
                    addStudent,
                    deleteStudent,
                    changeStudentName
                ) as T
            }
        }
    }
}