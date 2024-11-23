package com.tusur.teacherhelper.presentation.topicperformance

import androidx.lifecycle.viewModelScope
import com.google.android.material.R.attr
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.Student
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.usecase.GetNextStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetPossibleGradesUseCase
import com.tusur.teacherhelper.domain.usecase.GetPrevStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetStudentPerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetStudentUseCase
import com.tusur.teacherhelper.domain.usecase.GetSuggestedProgressForGradeUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicTypeByTopicUseCase
import com.tusur.teacherhelper.domain.usecase.SetStudentPerformanceUseCase
import com.tusur.teacherhelper.domain.util.inPercentage
import com.tusur.teacherhelper.presentation.core.model.Icon
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.core.util.formatProgress
import com.tusur.teacherhelper.presentation.core.util.toUiText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PerformanceViewModel.Factory::class)
class PerformanceViewModel @AssistedInject constructor(
    @Assisted("currentStudentId") currentStudentId: Int,
    @Assisted allStudentIds: List<Int>,
    @Assisted("topicId") private val topicId: Int,
    @Assisted private val datetimeMillis: Long,
    private val getStudent: GetStudentUseCase,
    private val getStudentPerformance: GetStudentPerformanceUseCase,
    private val getNextStudent: GetNextStudentUseCase,
    private val getPrevStudent: GetPrevStudentUseCase,
    private val getTopicType: GetTopicTypeByTopicUseCase,
    private val getPossibleGrades: GetPossibleGradesUseCase,
    private val setStudentPerformance: SetStudentPerformanceUseCase,
    private val getSuggestedProgressForGrade: GetSuggestedProgressForGradeUseCase
) : StudentPerformanceBaseViewModel(
    currentStudentId,
    allStudentIds,
    getStudent,
    getNextStudent,
    getPrevStudent,
) {
    private var topicType: TopicType? = null

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var gradeJustRemoved = false
    val shouldSetProgress
        get() = uiState.value.let {
            (it.isProgressAcceptable && it.progressPercent == 0)
                    || (gradeJustRemoved.also {
                gradeJustRemoved = false
            } && it.progressPercent != 0)
        }
    var suggestedProgressPercent = 0

    override fun fetch() {
        viewModelScope.launch {
            topicType = getTopicType(topicId)
            super.fetch()
        }
    }

    fun send(event: Event) {
        when (event) {
            Event.Fetch -> fetch()
            is Event.SetProgress -> setProgress(event.progressPercent)
            Event.SetNextStudent -> toNextStudentAttendance()
            Event.SetPrevStudent -> toPrevStudentAttendance()
        }
    }

    override suspend fun updateStudent(newStudent: Student?) {
        super.updateStudent(newStudent)
        val performance = getStudentPerformance(currentStudentId, topicId, datetimeMillis)
        _uiState.update { state ->
            state.copy(
                studentName = UiText.Dynamic(currentStudent!!.name.full),
                grade = performance.grade?.toUiText() ?: UiText.empty,
                progress = performance.progress?.toUiText() ?: UiText.empty,
                progressPercent = performance.progress?.inPercentage() ?: 0,
                assessmentPassed = performance.assessment == PerformanceItem.Assessment.PASS,
                assessmentSet = performance.assessment != null,
                isGradeAcceptable = topicType!!.isGradeAcceptable,
                isProgressAcceptable = topicType!!.isProgressAcceptable,
                isAssessmentAcceptable = topicType!!.isAssessmentAcceptable,
                hasNextStudent = nextStudent != null,
                hasPrevStudent = prevStudent != null,
                gradeItems = getAllGradeUiItems(performance.grade),
                assessmentItems = getAllAssessmentUiItems(performance.assessment),
                isFetched = true
            )
        }
    }

    private suspend fun saveProgress(progressPercent: Int) {
        setStudentPerformance(
            currentStudentId,
            topicId,
            PerformanceItem.Progress(progressPercent / 100f),
            datetimeMillis
        )
    }

    private fun setProgress(progressPercent: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    progressPercent = progressPercent,
                    progress = UiText.Dynamic(formatProgress(progressPercent))
                )
            }
            saveProgress(progressPercent)
        }
    }

    private suspend fun saveAssessment(assessment: PerformanceItem.Assessment?) {
        setStudentPerformance(
            currentStudentId,
            topicId,
            assessment,
            datetimeMillis
        )
    }

    private fun setAssessment(assessment: PerformanceItem.Assessment?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    assessmentSet = assessment != null,
                    assessmentPassed = assessment == PerformanceItem.Assessment.PASS
                )
            }
            saveAssessment(assessment)
        }
    }

    private suspend fun saveGrade(grade: PerformanceItem.Grade?) {
        setStudentPerformance(currentStudentId, topicId, grade, datetimeMillis)
    }

    private fun setGrade(grade: PerformanceItem.Grade?) {
        viewModelScope.launch {
            val uiState = uiState.value
            val prevGrade = uiState.gradeItems[uiState.checkedGradePosition].performanceItem
            gradeJustRemoved = grade == null && prevGrade != null
            suggestedProgressPercent = getSuggestedProgressForGrade(grade).inPercentage()
            _uiState.update {
                it.copy(grade = grade?.toUiText() ?: UiText.empty)
            }
            saveGrade(grade)
        }
    }

    private fun getAllGradeUiItems(grade: PerformanceItem.Grade?): List<PerformanceItemUiState> {
        val allGradesItem = getPossibleGrades().map { item ->
            PerformanceItemUiState(
                performanceItem = item,
                text = item.toUiText(),
                isSelected = item == grade,
                onCheck = { setGrade(item) }
            )
        }
        val noGradeItem = PerformanceItemUiState(
            performanceItem = null,
            text = UiText.Resource(R.string.grade_not_set),
            isSelected = grade == null,
            onCheck = { setGrade(null) }
        )
        return listOf(noGradeItem) + allGradesItem
    }

    private fun getAllAssessmentUiItems(checkedAssessment: PerformanceItem.Assessment?): List<PerformanceItemUiState> {
        return listOf(
            PerformanceItemUiState(
                performanceItem = null,
                text = UiText.Resource(R.string.assessment_not_set),
                isSelected = checkedAssessment == null,
                onCheck = { setAssessment(null) }
            ),
            PerformanceItemUiState(
                performanceItem = PerformanceItem.Assessment.PASS,
                text = UiText.Resource(R.string.assessment_pass),
                isSelected = checkedAssessment == PerformanceItem.Assessment.PASS,
                onCheck = { setAssessment(PerformanceItem.Assessment.PASS) }
            ),
            PerformanceItemUiState(
                performanceItem = PerformanceItem.Assessment.FAIL,
                text = UiText.Resource(R.string.assessment_fail),
                isSelected = checkedAssessment == PerformanceItem.Assessment.FAIL,
                onCheck = { setAssessment(PerformanceItem.Assessment.FAIL) }
            )
        )
    }

    data class UiState(
        val isFetched: Boolean = false,
        val studentName: UiText = UiText.empty,
        val grade: UiText = UiText.empty,
        val progress: UiText = UiText.empty,
        val progressPercent: Int = 0,
        val assessmentPassed: Boolean = false,
        val assessmentSet: Boolean = false,
        val isGradeAcceptable: Boolean = false,
        val isProgressAcceptable: Boolean = false,
        val isAssessmentAcceptable: Boolean = false,
        val hasNextStudent: Boolean = false,
        val hasPrevStudent: Boolean = false,
        val gradeItems: List<PerformanceItemUiState> = emptyList(),
        val assessmentItems: List<PerformanceItemUiState> = emptyList()
    ) {
        val checkedGradePosition
            get() = gradeItems.indexOfFirst { it.isSelected }.coerceAtLeast(0)
        val assessmentIcon: Icon? = if (!assessmentSet) {
            Icon(
                iconRes = R.drawable.ic_close_small_24,
                colorAttrRes = attr.colorError
            )
        } else if (assessmentPassed) {
            Icon(R.drawable.ic_check_small_24)
        } else {
            null
        }
    }

    data class PerformanceItemUiState(
        val performanceItem: PerformanceItem?,
        val text: UiText,
        val isSelected: Boolean,
        val onCheck: () -> Unit
    )

    sealed interface Event {
        data object Fetch : Event
        data class SetProgress(val progressPercent: Int) : Event
        data object SetPrevStudent : Event
        data object SetNextStudent : Event
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("currentStudentId") currentStudentId: Int,
            allStudentIds: List<Int>,
            @Assisted("topicId") topicId: Int,
            datetimeMillis: Long
        ): PerformanceViewModel
    }
}