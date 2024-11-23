package com.tusur.teacherhelper.presentation.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.ClassTime
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.domain.usecase.GetAllClassTimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetSharedClassTimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectByTopicIdUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectNotEmptyGroupsUseCase
import com.tusur.teacherhelper.presentation.core.model.UiText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel(assistedFactory = ClassTimeViewModel.Factory::class)
class ClassTimeViewModel @AssistedInject constructor(
    @Assisted private val topicId: Int,
    @Assisted private val groupIds: List<Int>?,
    @Assisted private val classDate: Date,
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

    @AssistedFactory
    interface Factory {
        fun create(topicId: Int, groupIds: List<Int>?, classDate: Date): ClassTimeViewModel
    }
}

data class ClassTimeItemUiState(
    val id: Int,
    val classTimeRange: UiText,
    val hasPerformance: Boolean,
    val onClick: () -> Unit
)