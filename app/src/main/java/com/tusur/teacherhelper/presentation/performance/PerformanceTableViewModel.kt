package com.tusur.teacherhelper.presentation.performance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.TableContent
import com.tusur.teacherhelper.domain.usecase.GetFinalPerformanceClassDayDatetimeMsUseCase
import com.tusur.teacherhelper.domain.usecase.GetGroupByIdUseCase
import com.tusur.teacherhelper.domain.usecase.GetGroupStudentsUseCase
import com.tusur.teacherhelper.domain.usecase.GetGroupSubjectPerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectStudentPerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectTopicsUseCase
import com.tusur.teacherhelper.domain.usecase.IsGroupNotEmptyUseCase
import com.tusur.teacherhelper.domain.usecase.SaveGroupPerformanceToExcelFileUseCase
import com.tusur.teacherhelper.domain.util.formattedShort
import com.tusur.teacherhelper.domain.util.shortName
import com.tusur.teacherhelper.presentation.App
import com.tusur.teacherhelper.presentation.model.UiText
import com.tusur.teacherhelper.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.util.Locale


class PerformanceTableViewModel(
    private val subjectId: Int,
    private val groupId: Int,
    private val locale: Locale,
    private val getGroup: GetGroupByIdUseCase,
    private val getSubjectTopics: GetSubjectTopicsUseCase,
    private val getGroupSubjectPerformance: GetGroupSubjectPerformanceUseCase,
    private val saveGroupPerformanceToFileTable: SaveGroupPerformanceToExcelFileUseCase,
    private val isGroupNotEmpty: IsGroupNotEmptyUseCase,
    private val getFinalPerformanceClassDayDatetimeMs: GetFinalPerformanceClassDayDatetimeMsUseCase,
    private val getGroupStudents: GetGroupStudentsUseCase
) : ViewModel() {

    private var studentsIds: List<Int> = emptyList()
    private var topicsIds: List<Int> = emptyList()

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _onetimeEventChannel = Channel<OnetimeEvent>()
    val onetimeEvent = _onetimeEventChannel.receiveAsFlow()


    fun fetch() {
        viewModelScope.launch {
            val group = getGroup(groupId)
            if (isGroupNotEmpty(groupId)) {
                val topics = getSubjectTopics(
                    subjectId = subjectId,
                    withCancelled = false
                ).first()
                val topicsText = topics.map { UiText.Dynamic(it.name.formattedShort(locale)) }
                topicsIds = topics.map { it.id }
                val labels =
                    listOf(UiText.Resource(R.string.student_name_abbreviation_label)) + topicsText

                getGroupSubjectPerformance(subjectId, groupId).distinctUntilChanged()
                    .collect { studentsPerformance ->
                        if (studentsPerformance.any { it.topicsWithPerformance.isNotEmpty() }) {
                            studentsIds = studentsPerformance.map { it.student.id }

                            TableContent(labels, studentsPerformance.count()) { rowIndex ->
                                val performance = studentsPerformance[rowIndex]
                                listOf(UiText.Dynamic(performance.student.shortName)) +
                                        performance.topicsWithPerformance.map { it.second.primaryPerformance() }
                            }.also { table ->
                                _uiState.update { it.copy(tableContent = table) }
                            }

                        } else {
                            _uiState.update { it.copy(thereAreNoNotCancelledTopics = true) }
                        }

                    }
            } else {
                _uiState.update { it.copy(thereAreNoNonEmptyGroups = true) }
            }

            _uiState.update {
                it.copy(group = UiText.Dynamic(group.number))
            }
        }
    }

    private fun getStudentId(clickedRowIndex: Int): Int {
        return studentsIds[clickedRowIndex]
    }

    private fun getTopicId(clickedColumnIndex: Int): Int {
        return topicsIds[clickedColumnIndex - 1]
    }

    fun savePerformanceToFile(
        outputStream: OutputStream,
        context: Context,
        onSaveSucceed: (mime: String) -> Unit
    ) {
        viewModelScope.launch {
            val mime = saveGroupPerformanceToFileTable(
                outputStream,
                uiState.value.tableContent
            ) { uiText ->
                uiText.toString(context)
            }
            onSaveSucceed(mime)
        }
    }

    var showFullName: Boolean = false
        set(value) {
            if (value != field) {
                field = value
            }
            if (uiState.value.tableContent.isEmpty()) return
            viewModelScope.launch {
                val studentNames = getGroupStudents(groupId).first().let { students ->
                    if (field) {
                        students.map { UiText.Dynamic(it.name.full) }
                    } else {
                        students.map { UiText.Dynamic(it.shortName) }
                    }
                }

                _uiState.update {
                    it.copy(tableContent = it.tableContent.editContent { rowIndex, columnIndex, value ->
                        if (columnIndex == 0) {
                            studentNames[rowIndex]
                        } else {
                            value
                        }
                    })
                }
            }
        }

    private fun Performance.primaryPerformance(): UiText {
        return grade?.toUiText() ?: progress?.toUiText() ?: UiText.empty
    }

    fun fetchClickedInfo(columnIndex: Int, rowIndex: Int) {
        viewModelScope.launch {
            val studentId = getStudentId(rowIndex)
            if (columnIndex == 0) {
                _onetimeEventChannel.send(OnetimeEvent.StudentClick(studentId))
            } else {
                val topicId = getTopicId(columnIndex)
                val classDatetimeMs = getFinalPerformanceClassDayDatetimeMs(studentId, topicId)!!
                _onetimeEventChannel.send(
                    OnetimeEvent.PerformanceClick(
                        studentId = studentId,
                        topicId = topicId,
                        datetimeMs = classDatetimeMs
                    )
                )
            }
        }
    }

    data class UiState(
        val group: UiText = UiText.empty,
        val tableContent: TableContent<UiText> = TableContent.empty(),
        val thereAreNoNonEmptyGroups: Boolean = false,
        val thereAreNoNotCancelledTopics: Boolean = false
    )

    sealed interface OnetimeEvent {
        data class PerformanceClick(
            val studentId: Int,
            val topicId: Int,
            val datetimeMs: Long
        ) : OnetimeEvent

        data class StudentClick(val studentId: Int) : OnetimeEvent
    }

    companion object {
        fun factory(subjectId: Int, groupId: Int, locale: Locale) =
            viewModelFactory {
                initializer {
                    PerformanceTableViewModel(
                        subjectId,
                        groupId,
                        locale,
                        GetGroupByIdUseCase(App.module.groupRepository),
                        GetSubjectTopicsUseCase(App.module.topicRepository),
                        GetGroupSubjectPerformanceUseCase(
                            GetSubjectStudentPerformanceUseCase(
                                App.module.studentPerformanceRepository,
                                App.module.topicRepository
                            ),
                            App.module.studentRepository
                        ),
                        SaveGroupPerformanceToExcelFileUseCase(),
                        IsGroupNotEmptyUseCase(App.module.groupRepository),
                        GetFinalPerformanceClassDayDatetimeMsUseCase(
                            App.module.studentPerformanceRepository
                        ),
                        GetGroupStudentsUseCase(App.module.studentRepository)
                    )
                }
            }
    }
}
