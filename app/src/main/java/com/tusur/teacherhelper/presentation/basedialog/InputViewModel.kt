package com.tusur.teacherhelper.presentation.basedialog

import androidx.lifecycle.ViewModel
import com.tusur.teacherhelper.presentation.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class InputViewModel : ViewModel() {

    protected val mutableUiState = MutableStateFlow(UiState())
    val uiState = mutableUiState.asStateFlow()

    abstract fun send(event: Event)

    data class UiState(
        val isSavedSuccessfully: Boolean = false,
        val error: UiText? = null,
        val savedItemId: Int? = null,
        val currentText: CharSequence = ""
    )

    sealed interface Event {
        data class TryAdd(val text: String) : Event
        data class Input(val text: String) : Event
    }
}
