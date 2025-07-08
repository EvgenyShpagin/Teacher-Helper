package com.tusur.teacherhelper.presentation.topicperformance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.domain.usecase.DeletePerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetSharedClassDatetimeUseCase
import com.tusur.teacherhelper.domain.util.toEpochMillis
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.core.util.formatted
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

@HiltViewModel(assistedFactory = SharedClassDatesViewModel.Factory::class)
class SharedClassDatesViewModel @AssistedInject constructor(
    @Assisted private val topicId: Int,
    @Assisted private val groupListIds: List<Int>,
    private val getSharedClassDays: GetSharedClassDatetimeUseCase,
    private val deletePerformance: DeletePerformanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _onetimeEvent = Channel<OnetimeEvent>()
    val onetimeEvent = _onetimeEvent.receiveAsFlow()

    fun fetch() {
        viewModelScope.launch {
            val sharedClassDates = getSharedClassDays(topicId = topicId, groupsIds = groupListIds)

            val classDateTimeOfEach = mutableMapOf<DateItemUiState, List<TimeItemUiState>>()

            sharedClassDates.forEach { datetime ->
                val date = datetime.date.toUiItem()
                val time = datetime.time.toUiItem(datetime)
                if (date in classDateTimeOfEach) {
                    classDateTimeOfEach[date] = classDateTimeOfEach[date]!! + time
                } else {
                    classDateTimeOfEach[date] = listOf(time)
                }
            }

            _uiState.update { state ->
                state.copy(
                    sharedClassDates = sharedClassDates.map { it.date.toUiItem() }.distinct(),
                    classDateTimeOfEach = classDateTimeOfEach,
                    isFetched = true
                )
            }
        }
    }

    private suspend fun deleteAttendance(datetime: LocalDateTime) {
        deletePerformance(
            topicId = topicId,
            groupListIds = groupListIds,
            datetime = datetime
        )
    }

    fun startDelete() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
        }
    }

    fun stopDelete() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = false) }
        }
    }

    private fun LocalDate.toUiItem(): DateItemUiState {
        return DateItemUiState(
            dateMillis = toEpochMillis(),
            dateText = UiText.Dynamic(formatted())
        )
    }

    private fun LocalTime.toUiItem(datetime: LocalDateTime): TimeItemUiState {
        return TimeItemUiState(
            timeText = UiText.Dynamic(formatted()),
            onDelete = {
                viewModelScope.launch {
                    deleteAttendance(datetime)
                    stopDelete()
                    _onetimeEvent.send(OnetimeEvent.ClassDateDeleted)
                }
            }
        )
    }

    sealed interface OnetimeEvent {
        data object ClassDateDeleted : OnetimeEvent
    }

    data class UiState(
        val isFetched: Boolean = false,
        val sharedClassDates: List<DateItemUiState> = emptyList(),
        val classDateTimeOfEach: Map<DateItemUiState, List<TimeItemUiState>> = emptyMap(),
        val isDeleting: Boolean = false
    )

    @AssistedFactory
    interface Factory {
        fun create(topicId: Int, groupListIds: List<Int>): SharedClassDatesViewModel
    }
}

data class DateItemUiState(
    val dateMillis: Long,
    val dateText: UiText
)

data class TimeItemUiState(
    val timeText: UiText,
    val onDelete: () -> Unit
)