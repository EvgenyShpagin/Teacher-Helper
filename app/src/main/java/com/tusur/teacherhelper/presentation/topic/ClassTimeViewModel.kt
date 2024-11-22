package com.tusur.teacherhelper.presentation.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.ClassTime
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.domain.usecase.GetAllClassTimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetSharedClassDatetimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetSharedClassTimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectByTopicIdUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectNotEmptyGroupsUseCase
import com.tusur.teacherhelper.presentation.core.App
import com.tusur.teacherhelper.presentation.core.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ClassTimeViewModel(
    private val topicId: Int,
    private val groupIds: List<Int>?,
    private val classDate: Date,
    private val getAllClassTime: GetAllClassTimeUseCase,
    private val getSharedClassTime: GetSharedClassTimeUseCase,
    private val getSubjectNotEmptyGroups: GetSubjectNotEmptyGroupsUseCase,
    private val getSubjectByTopicId: GetSubjectByTopicIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()


    fun fetch() {
        viewModelScope.launch {
            val allClassTime = getAllClassTime()
            val groupsIds = groupIds ?: getSubjectNotEmptyGroups(
                subjectId = getSubjectByTopicId(topicId).id
            ).map { it.id }
            val sharedClassTimeList = getSharedClassTime(topicId, groupsIds, classDate)
            _uiState.update {
                UiState(
                    chosenTimeMillis = null,
                    itemsUiState = allClassTime.map { time ->
                        time.toUiItem(hasPerformance = time in sharedClassTimeList)
                    }
                )
            }
        }
    }

    private fun ClassTime.toUiItem(hasPerformance: Boolean) = ClassTimeItemUiState(
        id = hashCode(),
        classTimeRange = UiText.Resource(
            resId = R.string.class_time_range,
            args = arrayOf(initTime.hour, initTime.minute, finishTime.hour, finishTime.minute),
        ),
        hasPerformance = hasPerformance,
        onClick = { _uiState.update { it.copy(chosenTimeMillis = initTime.toMillis()) } }
    )

    data class UiState(
        val chosenTimeMillis: Long? = null,
        val lastSelectedItem: ClassTimeItemUiState? = null,
        val itemsUiState: List<ClassTimeItemUiState> = emptyList(),
        val extraItemDescription: UiText = UiText.empty
    )

    companion object {
        fun factory(topicId: Int, groupIds: List<Int>?, classDate: Date) = viewModelFactory {
            initializer {
                ClassTimeViewModel(
                    topicId = topicId,
                    groupIds = groupIds,
                    classDate = classDate,
                    getAllClassTime = GetAllClassTimeUseCase(App.module.classTimeRepository),
                    getSharedClassTime = GetSharedClassTimeUseCase(
                        GetSharedClassDatetimeUseCase(App.module.classDateRepository),
                        GetAllClassTimeUseCase(App.module.classTimeRepository)
                    ),
                    getSubjectNotEmptyGroups = GetSubjectNotEmptyGroupsUseCase(
                        App.module.subjectGroupRepository,
                        App.module.groupRepository
                    ),
                    getSubjectByTopicId = GetSubjectByTopicIdUseCase(
                        App.module.subjectRepository
                    )
                )
            }
        }
    }
}

data class ClassTimeItemUiState(
    val id: Int,
    val classTimeRange: UiText,
    val hasPerformance: Boolean,
    val onClick: () -> Unit
)