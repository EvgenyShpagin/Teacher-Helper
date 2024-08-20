package com.tusur.teacherhelper.presentation.topictype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Result
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.model.error.TopicTypeDeleteError
import com.tusur.teacherhelper.domain.usecase.DeleteTopicTypeUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicTypesUseCase
import com.tusur.teacherhelper.domain.usecase.IsTopicTypeBaseUseCase
import com.tusur.teacherhelper.domain.usecase.IsTopicTypeUsedByAnyTopicUseCase
import com.tusur.teacherhelper.presentation.App
import com.tusur.teacherhelper.presentation.model.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class TopicTypesViewModel(
    private val getTopicTypes: GetTopicTypesUseCase,
    private val deleteTopicType: DeleteTopicTypeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()


    fun fetch() {
        viewModelScope.launch {
            getTopicTypes().flowOn(Dispatchers.IO).collect { types ->
                _uiState.update { state ->
                    state.copy(topicTypeItemsUiState = types.map { it.toUiItem() })
                }
            }
        }
    }

    fun deleteType(typeId: Int, onDeleteError: (reason: UiText) -> Unit) {
        viewModelScope.launch {
            val deleteResult = deleteTopicType(typeId)
            if (deleteResult is Result.Error) {
                val reasonText = UiText.Resource(
                    when (deleteResult.error) {
                        TopicTypeDeleteError.CANNOT_DELETE_BASE_TYPES ->
                            R.string.dialog_delete_base_error_topic_type_body

                        TopicTypeDeleteError.USED_BY_SOME_TOPICS ->
                            R.string.dialog_delete_used_error_topic_type_body
                    }
                )
                onDeleteError(reasonText)
            }
            stopDelete()
        }
    }

    fun startDelete() {
        _uiState.update {
            it.copy(isDeleting = true)
        }
    }

    fun stopDelete() {
        _uiState.update {
            it.copy(isDeleting = false)
        }
    }

    private fun TopicType.toUiItem() = TopicTypeItemUiState(
        typeId = id, name = UiText.Dynamic(name)
    )

    data class UiState(
        val topicTypeItemsUiState: List<TopicTypeItemUiState> = emptyList(),
        val isDeleting: Boolean = false
    )

    companion object {
        val factory = object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TopicTypesViewModel(
                    getTopicTypes = GetTopicTypesUseCase(App.module.topicTypeRepository),
                    deleteTopicType = DeleteTopicTypeUseCase(
                        App.module.topicTypeRepository,
                        IsTopicTypeBaseUseCase(),
                        IsTopicTypeUsedByAnyTopicUseCase(App.module.topicTypeRepository)
                    )
                ) as T
            }
        }
    }
}

data class TopicTypeItemUiState(
    val typeId: Int,
    val name: UiText
)