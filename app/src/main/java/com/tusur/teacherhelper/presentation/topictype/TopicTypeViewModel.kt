package com.tusur.teacherhelper.presentation.topictype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.usecase.CreateTopicTypeUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicTypeUseCase
import com.tusur.teacherhelper.domain.usecase.IsShortTypeNameRequiredUseCase
import com.tusur.teacherhelper.domain.usecase.UpdateTopicTypeUseCase
import com.tusur.teacherhelper.presentation.App
import com.tusur.teacherhelper.presentation.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class TopicTypeViewModel(
    private val isCreating: Boolean,
    private val typeId: Int,
    private val getTopicType: GetTopicTypeUseCase,
    private val isShortTypeNameRequired: IsShortTypeNameRequiredUseCase,
    private val createTopicType: CreateTopicTypeUseCase,
    private val updateTopicType: UpdateTopicTypeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()


    fun fetch() {
        viewModelScope.launch {
            val topicType = getTopicType(typeId)
            _uiState.update { currentState ->
                getUpdatedState(currentState, topicType)
            }
        }
    }

    fun send(event: Event) {
        when (event) {
            Event.CheckAssessment -> checkAssessment()
            Event.CheckDeadline -> checkDeadline()
            Event.CheckGrade -> checkGrade()
            Event.CheckMultipleDaysAttendance -> checkMultipleAttendance()
            Event.CheckAttendance -> checkAttendance()
            Event.CheckOneDayAttendance -> checkSingleAttendance()
            Event.CheckProgress -> checkProgress()
            is Event.NameUpdate -> updateName(event.text)
            is Event.ShortNameUpdate -> updateShortName(event.text)
            Event.SaveChanges -> save()
        }
    }

    private fun getUpdatedState(currentState: UiState, topicType: TopicType?): UiState {
        return if (topicType != null) {
            currentState.copy(
                title = UiText.Resource(
                    if (isCreating) {
                        R.string.topic_type_creating_title
                    } else {
                        R.string.topic_type_editing_title
                    }
                ),
                typeName = UiText.Dynamic(topicType.name),
                typeShortName = UiText.Dynamic(topicType.shortName),
                needToShowShortNameInput = isShortTypeNameRequired(topicType.name),
                assessmentAcceptable = topicType.isAssessmentAcceptable,
                gradeAcceptable = topicType.isGradeAcceptable,
                progressAcceptable = topicType.isProgressAcceptable,
                attendanceAcceptable = topicType.isAttendanceAcceptable,
                attendanceOnlyOneDayAcceptable = topicType.isAttendanceForOneClassOnly,
                deadlineAcceptable = topicType.canDeadlineBeSpecified,
                allowedToSave = !isCreating
            )
        } else {
            currentState.copy(
                title = UiText.Resource(
                    if (isCreating) {
                        R.string.topic_type_creating_title
                    } else {
                        R.string.topic_type_editing_title
                    }
                )
            )
        }
    }

    private fun updateName(originName: String?) = _uiState.update { state ->
        state.copy(
            typeName = UiText.Dynamic(originName ?: ""),
            needToShowShortNameInput = isShortTypeNameRequired(originName)
        ).also { checkSaveAbility(it) }
    }

    private fun updateShortName(shortName: String?) = _uiState.update { state ->
        state.copy(typeShortName = UiText.Dynamic(shortName ?: ""))
            .also { checkSaveAbility(it) }
    }

    private fun checkAssessment() = _uiState.update { state ->
        state.copy(
            assessmentAcceptable = !state.assessmentAcceptable,
            gradeAcceptable = false
        ).also { checkSaveAbility(it) }
    }

    private fun checkDeadline() = _uiState.update {
        it.copy(deadlineAcceptable = !it.deadlineAcceptable)
    }

    private fun checkGrade() = _uiState.update { state ->
        state.copy(
            gradeAcceptable = !state.gradeAcceptable,
            assessmentAcceptable = false
        ).also { checkSaveAbility(it) }
    }

    private fun checkProgress() = _uiState.update { state ->
        state.copy(progressAcceptable = !state.progressAcceptable)
            .also { checkSaveAbility(it) }
    }

    private fun checkAttendance() = _uiState.update { state ->
        state.copy(attendanceAcceptable = !state.attendanceAcceptable)
            .also { checkSaveAbility(it) }
    }

    private fun checkMultipleAttendance() = _uiState.update { state ->
        state.copy(
            attendanceAcceptable = true,
            attendanceOnlyOneDayAcceptable = false
        )
    }

    private fun checkSingleAttendance() = _uiState.update {
        it.copy(
            attendanceAcceptable = true,
            attendanceOnlyOneDayAcceptable = true
        )
    }

    private fun save() {
        viewModelScope.launch {
            uiState.value.apply {
                val topicType = TopicType(
                    id = typeId,
                    name = (typeName as UiText.Dynamic).value,
                    shortName = (typeShortName as UiText.Dynamic).value,
                    canDeadlineBeSpecified = deadlineAcceptable,
                    isGradeAcceptable = gradeAcceptable,
                    isProgressAcceptable = progressAcceptable,
                    isAssessmentAcceptable = assessmentAcceptable,
                    isAttendanceAcceptable = attendanceAcceptable,
                    isAttendanceForOneClassOnly = attendanceOnlyOneDayAcceptable
                )
                if (isCreating) {
                    createTopicType(topicType)
                } else {
                    updateTopicType(topicType)
                }
            }
        }
    }

    private fun checkSaveAbility(currentState: UiState) = currentState.apply {
        val anythingAcceptable = gradeAcceptable || assessmentAcceptable
                || progressAcceptable || attendanceAcceptable
        val canSave = anythingAcceptable && if (needToShowShortNameInput) {
            (typeShortName as UiText.Dynamic).value.isNotBlank()
        } else {
            (typeName as UiText.Dynamic).value.isNotBlank()
        }
        if (allowedToSave xor canSave) {
            _uiState.update { it.copy(allowedToSave = canSave) }
        }
    }

    data class UiState(
        val title: UiText = UiText.empty,
        val typeName: UiText = UiText.empty,
        val typeShortName: UiText = UiText.empty,
        val needToShowShortNameInput: Boolean = false,
        val assessmentAcceptable: Boolean = false,
        val gradeAcceptable: Boolean = false,
        val progressAcceptable: Boolean = false,
        val attendanceAcceptable: Boolean = false,
        val attendanceOnlyOneDayAcceptable: Boolean = false,
        val deadlineAcceptable: Boolean = false,
        val allowedToSave: Boolean = false
    )

    sealed interface Event {
        data object CheckAssessment : Event
        data object CheckProgress : Event
        data object CheckGrade : Event
        data object CheckAttendance : Event
        data object CheckOneDayAttendance : Event
        data object CheckMultipleDaysAttendance : Event
        data object CheckDeadline : Event
        data object SaveChanges : Event
        data class NameUpdate(val text: String?) : Event
        data class ShortNameUpdate(val text: String?) : Event
    }

    companion object {
        fun factory(isCreating: Boolean, typeId: Int) = object : ViewModelProvider.Factory {
            private val getTopicType = GetTopicTypeUseCase(App.module.topicTypeRepository)
            private val isShortTypeNameRequired = IsShortTypeNameRequiredUseCase()
            private val createTopicType = CreateTopicTypeUseCase(App.module.topicTypeRepository)
            private val updateTopicType = UpdateTopicTypeUseCase(App.module.topicTypeRepository)

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TopicTypeViewModel(
                    isCreating = isCreating,
                    typeId = typeId,
                    getTopicType = getTopicType,
                    isShortTypeNameRequired = isShortTypeNameRequired,
                    createTopicType = createTopicType,
                    updateTopicType = updateTopicType
                ) as T
            }
        }
    }
}