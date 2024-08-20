package com.tusur.teacherhelper.presentation.topicperformance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.domain.model.Datetime
import com.tusur.teacherhelper.domain.usecase.DeletePerformanceUseCase
import com.tusur.teacherhelper.domain.usecase.GetSharedClassDatesUseCase
import com.tusur.teacherhelper.domain.usecase.GetSharedClassDatetimeUseCase
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.presentation.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class SharedClassDatesViewModel(
    private val topicId: Int,
    private val locale: Locale,
    private val groupListIds: List<Int>,
    private val getSharedClassDays: GetSharedClassDatesUseCase,
    private val deletePerformance: DeletePerformanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun fetch() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    sharedClassDays = getSharedClassDays(
                        topicId = topicId,
                        groupsIds = groupListIds
                    ).map { it.toUiItem() },
                    isFetched = true
                )
            }
        }
    }

    fun deleteAttendance(dateItemUiState: DateItemUiState) {
        viewModelScope.launch {
            deletePerformance(
                topicId = topicId,
                groupListIds = groupListIds,
                datetime = Datetime.fromMillis(dateItemUiState.dateMillis)
            )
        }
    }

    fun startDelete() {
        viewModelScope.launch { _uiState.update { it.copy(isDeleting = true) } }
    }

    fun stopDelete() {
        viewModelScope.launch { _uiState.update { it.copy(isDeleting = false) } }
    }

    private fun Date.toUiItem() = DateItemUiState(toMillis(), formatted(locale))

    data class UiState(
        val isFetched: Boolean = false,
        val sharedClassDays: List<DateItemUiState> = emptyList(),
        val isDeleting: Boolean = false
    )

    companion object {

        fun factory(topicId: Int, locale: Locale, groupListIds: List<Int>) =
            viewModelFactory {
                initializer {
                    SharedClassDatesViewModel(
                        topicId,
                        locale,
                        groupListIds,
                        GetSharedClassDatesUseCase(
                            GetSharedClassDatetimeUseCase(App.module.classDateRepository)
                        ),
                        DeletePerformanceUseCase(App.module.studentPerformanceRepository)
                    )
                }
            }
    }
}

data class DateItemUiState(
    val dateMillis: Long,
    val dateText: String
)