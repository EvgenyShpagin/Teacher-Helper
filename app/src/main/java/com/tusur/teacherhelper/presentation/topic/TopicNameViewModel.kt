package com.tusur.teacherhelper.presentation.topic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.TopicType
import com.tusur.teacherhelper.domain.model.error.TopicNameError
import com.tusur.teacherhelper.domain.usecase.CanTopicTypeBeReplacedUseCase
import com.tusur.teacherhelper.domain.usecase.CheckTopicNameAddTextUseCase
import com.tusur.teacherhelper.domain.usecase.CreateSubjectTopicUseCase
import com.tusur.teacherhelper.domain.usecase.GetAvailableTopicOrdinalUseCase
import com.tusur.teacherhelper.domain.usecase.GetPrimaryTopicTypesUseCase
import com.tusur.teacherhelper.domain.usecase.GetSecondaryTopicTypesUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicByNameUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicTypeUseCase
import com.tusur.teacherhelper.domain.usecase.GetTopicUseCase
import com.tusur.teacherhelper.domain.usecase.UpdateSubjectTopicUseCase
import com.tusur.teacherhelper.domain.usecase.ValidateTopicNameUseCase
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.domain.util.Result
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.presentation.core.App
import com.tusur.teacherhelper.presentation.core.model.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale


class TopicNameViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val locale: Locale,
    private val topicId: Int,
    private val subjectId: Int,
    private val createSubjectTopic: CreateSubjectTopicUseCase,
    private val getAvailableTopicOrdinal: GetAvailableTopicOrdinalUseCase,
    private val getPrimaryTopicTypes: GetPrimaryTopicTypesUseCase,
    private val getSecondaryTopicTypes: GetSecondaryTopicTypesUseCase,
    private val updateSubjectTopic: UpdateSubjectTopicUseCase,
    private val getTopic: GetTopicUseCase,
    private val getTopicType: GetTopicTypeUseCase,
    private val validateTopicName: ValidateTopicNameUseCase,
    private val canTopicTypeBeReplaced: CanTopicTypeBeReplacedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _onetimeEvents = Channel<OnetimeEvent>()
    val onetimeEvents = _onetimeEvents.receiveAsFlow()

    private var topic: Topic? = null

    private val isCreatingTopic = topicId == NO_ID

    fun send(event: Event) {
        when (event) {
            Event.Fetch -> fetch()
            Event.Save -> save()
            is Event.SetDate -> setDate(event.date)
            is Event.SetAddText -> setAdditionalText(event.text)
            Event.AddOrRemoveOrdinal -> if (uiState.value.ordinal == null) {
                addOrdinal()
            } else {
                removeAdditionalOrdinal()
            }
        }
    }

    private fun fetch() {
        viewModelScope.launch {
            topic = getTopic(topicId)
            if (savedStateHandle.get<Boolean>(KEY_WAS_MODIFIED) == true) {
                restoreSavedUiState()
            } else {
                setupInitUiState()
            }
        }
    }

    private fun addOrdinal() = viewModelScope.launch {
        val topicTypeId = savedStateHandle.get<Int>(KEY_CHECKED_TYPE_ID)!!
        val newOrdinal = getAvailableTopicOrdinal(
            subjectId = subjectId,
            topicId = topicId,
            topicTypeId = topicTypeId
        )
        savedStateHandle[KEY_ORDINAL] = newOrdinal
        _uiState.update { it.copy(ordinal = UiText.Dynamic(newOrdinal.toString())) }
    }

    private fun removeAdditionalOrdinal() {
        savedStateHandle.remove<Int>(KEY_ORDINAL)
        _uiState.update { it.copy(ordinal = null) }
    }

    private fun setDate(date: Date?) {
        savedStateHandle[KEY_DATE_MS] = date?.toMillis()
        _uiState.update { uiState ->
            uiState.copy(date = date?.formatted(locale)?.let { UiText.Dynamic(it) })
        }
    }

    private fun save() = viewModelScope.launch {
        val topicType = getTopicType(savedStateHandle.get<Int>(KEY_CHECKED_TYPE_ID)!!)!!
        val topicName = Topic.Name(
            typeName = topicType.name,
            shortTypeName = topicType.shortName,
            addText = savedStateHandle[KEY_ADD_TEXT],
            ordinal = savedStateHandle[KEY_ORDINAL],
            date = savedStateHandle.get<Long>(KEY_DATE_MS)?.let { Date.fromMillis(it) }
        )
        _onetimeEvents.send(
            when (val result = validateTopicName(
                subjectId = subjectId,
                topicId = topicId,
                topicTypeId = topicType.id,
                name = topicName
            )) {
                is Result.Success -> {
                    if (isCreatingTopic) {
                        OnetimeEvent.SaveSuccess(
                            isJustCreated = true,
                            createdTopicId = createSubjectTopic(subjectId, topicType, topicName)
                        )
                    } else {
                        updateSubjectTopic(
                            topic = topic!!.copy(name = topicName, type = topicType)
                        )
                        OnetimeEvent.SaveSuccess(isJustCreated = false)
                    }
                }

                is Result.Error -> OnetimeEvent.SaveFailed(
                    message = when (result.error) {
                        TopicNameError.NOT_CHANGED ->
                            UiText.Resource(R.string.topic_name_not_changed_error)

                        TopicNameError.ALREADY_EXISTS ->
                            UiText.Resource(R.string.topic_name_already_exists_error)
                    }
                )
            }
        )
    }

    private fun setAdditionalText(text: String?) {
        savedStateHandle[KEY_ADD_TEXT] = text
        _uiState.update { uiState ->
            uiState.copy(addText = text?.let { UiText.Dynamic(it) })
        }
    }

    private suspend fun restoreSavedUiState() {
        val savedTypeId = savedStateHandle.get<Int>(KEY_SELECTED_TYPE_ID)
        val savedType = savedTypeId?.let { getTopicType(it) }

        val primaryUiItems = getPrimaryTopicTypes().map {
            it.toUiItem(
                isEnabled = isCreatingTopic ||
                        savedType != null && canTopicTypeBeReplaced(savedType, it),
                isSelected = it.id == savedTypeId
            )
        }

        val secondaryUiItems = getSecondaryTopicTypes().map {
            it.toUiItem(
                isEnabled = isCreatingTopic ||
                        savedType != null && canTopicTypeBeReplaced(savedType, it),
                isSelected = it.id == savedTypeId
            )
        }

        _uiState.update { uiState ->
            uiState.copy(
                isCreating = topic == null,
                topicTypesItems = wrapPrimaryTopicTypes(primaryUiItems, secondaryUiItems),
                secondaryTopicTypesItems = secondaryUiItems,
                addText = savedStateHandle.get<String>(KEY_ADD_TEXT)?.let { UiText.Dynamic(it) },
                ordinal = savedStateHandle[KEY_ORDINAL],
                date = savedStateHandle.get<Long>(KEY_DATE_MS)
                    ?.let { UiText.Dynamic(Date.fromMillis(it).formatted(locale)) }
            )
        }
    }

    private suspend fun setupInitUiState() {
        val topic = topic
        if (topic == null) {
            val primaryUiItems = getPrimaryTopicTypes().map { it.toUiItem(isEnabled = true) }
            val secondaryUiItems = getSecondaryTopicTypes().map { it.toUiItem(isEnabled = true) }
            _uiState.update { uiState ->
                uiState.copy(
                    isCreating = true,
                    topicTypesItems = wrapPrimaryTopicTypes(primaryUiItems, secondaryUiItems),
                    secondaryTopicTypesItems = secondaryUiItems
                )
            }
        } else {
            savedStateHandle[KEY_CHECKED_TYPE_ID] = topic.type.id
            val currentType = getTopicType(topic.type.id)!!
            val primaryUiItems = getPrimaryTopicTypes().map {
                it.toUiItem(
                    isEnabled = isCreatingTopic || canTopicTypeBeReplaced(currentType, it),
                    isSelected = it.id == topic.type.id
                )
            }
            val secondaryUiItems = getSecondaryTopicTypes().map {
                it.toUiItem(
                    isEnabled = isCreatingTopic || canTopicTypeBeReplaced(currentType, it),
                    isSelected = it.id == topic.type.id
                )
            }
            _uiState.update { uiState ->
                uiState.copy(
                    isCreating = false,
                    topicTypesItems = wrapPrimaryTopicTypes(primaryUiItems, secondaryUiItems),
                    secondaryTopicTypesItems = secondaryUiItems,
                    addText = topic.name.addText?.also { savedStateHandle[KEY_ADD_TEXT] = it }
                        ?.let { UiText.Dynamic(it) },
                    ordinal = topic.name.ordinal?.also { savedStateHandle[KEY_ORDINAL] = it }
                        ?.let { UiText.Dynamic(it.toString()) },
                    date = topic.name.date?.also { savedStateHandle[KEY_DATE_MS] = it.toMillis() }
                        ?.let { UiText.Dynamic(it.formatted(locale)) }
                )
            }
        }
    }

    private fun selectType(checkedTypeId: Int) = viewModelScope.launch {
        val topicType = getTopicType(checkedTypeId)!!
        savedStateHandle[KEY_CHECKED_TYPE_ID] = checkedTypeId
        _uiState.update { state ->
            state.copy(
                topicTypesItems = state.topicTypesItems.getWithChecked(
                    if (state.secondaryTopicTypesItems.any { it.typeId == checkedTypeId }) {
                        NO_ID
                    } else {
                        checkedTypeId
                    }
                ),
                secondaryTopicTypesItems = state.secondaryTopicTypesItems
                    .getSecondaryWithChecked(checkedTypeId),
                ordinal = if (state.ordinal != null) {
                    getAvailableTopicOrdinal(subjectId, topicId, topicType.id)
                        .let { UiText.Dynamic(it.toString()) }
                } else {
                    null
                }
            )
        }
    }

    private fun TopicType.toUiItem(
        isEnabled: Boolean,
        isSelected: Boolean = false
    ): TopicTypeItemUiState.Type {
        return TopicTypeItemUiState.Type(
            typeId = id,
            name = UiText.Dynamic(name),
            isSelected = isSelected,
            isEnabled = isEnabled,
            onSelect = { selectType(id) })
    }

    private fun List<TopicTypeItemUiState>.getWithChecked(
        checkedTypeId: Int
    ): List<TopicTypeItemUiState> {
        return map {
            when (it) {
                is TopicTypeItemUiState.Label -> it
                is TopicTypeItemUiState.Type -> {
                    if (it.typeId == checkedTypeId) {
                        it.copy(isSelected = true)
                    } else if (it.isSelected) {
                        it.copy(isSelected = false)
                    } else {
                        it
                    }
                }
            }
        }
    }

    private fun List<TopicTypeItemUiState.Type>.getSecondaryWithChecked(
        checkedTypeId: Int
    ): List<TopicTypeItemUiState.Type> {
        return map {
            if (it.typeId == checkedTypeId) {
                it.copy(isSelected = true)
            } else if (it.isSelected) {
                it.copy(isSelected = false)
            } else {
                it
            }
        }
    }

    private fun wrapPrimaryTopicTypes(
        types: List<TopicTypeItemUiState>,
        secondaryTopicTypes: List<TopicTypeItemUiState.Type>
    ): List<TopicTypeItemUiState> {
        return listOf(TopicTypeItemUiState.Label(UiText.Resource(R.string.topic_type_label))) +
                types + secondaryTopicTypes.getSingleSecondaryTypeItem()
    }

    private fun List<TopicTypeItemUiState.Type>.getSingleSecondaryTypeItem(): TopicTypeItemUiState.Type {
        return TopicTypeItemUiState.Type(
            typeId = NO_ID,
            name = UiText.Resource(R.string.topic_type_other),
            isSelected = any { it.isSelected },
            isEnabled = any { it.isEnabled },
            onSelect = {
                viewModelScope.launch { _onetimeEvents.send(OnetimeEvent.OtherTypesClick) }
            }
        )
    }

    data class UiState(
        val isCreating: Boolean = true,
        val topicTypesItems: List<TopicTypeItemUiState> = emptyList(),
        val secondaryTopicTypesItems: List<TopicTypeItemUiState.Type> = emptyList(),
        val addText: UiText? = null,
        val ordinal: UiText? = null,
        val date: UiText? = null,
    ) {
        val checkedTopicName = secondaryTopicTypesItems.find { it.isSelected }?.name
            ?: topicTypesItems.filterIsInstance<TopicTypeItemUiState.Type>()
                .find { it.isSelected }?.name ?: UiText.empty

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UiState

            if (isCreating != other.isCreating) return false
            if (topicTypesItems != other.topicTypesItems) return false
            if (secondaryTopicTypesItems != other.secondaryTopicTypesItems) return false
            if (addText != other.addText) return false
            if (ordinal != other.ordinal) return false
            if (date != other.date) return false
            if (checkedTopicName != other.checkedTopicName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = isCreating.hashCode()
            result = 31 * result + topicTypesItems.hashCode()
            result = 31 * result + secondaryTopicTypesItems.hashCode()
            result = 31 * result + (addText?.hashCode() ?: 0)
            result = 31 * result + (ordinal?.hashCode() ?: 0)
            result = 31 * result + (date?.hashCode() ?: 0)
            return result
        }
    }

    // From user
    sealed interface Event {
        data object Fetch : Event
        data object Save : Event
        data class SetAddText(val text: String?) : Event
        data object AddOrRemoveOrdinal : Event
        data class SetDate(val date: Date?) : Event
    }

    // To user
    sealed interface OnetimeEvent {
        data class SaveFailed(val message: UiText) : OnetimeEvent
        data class SaveSuccess(
            val isJustCreated: Boolean = false,
            val createdTopicId: Int? = null
        ) : OnetimeEvent

        data object OtherTypesClick : OnetimeEvent
    }

    companion object {

        private const val KEY_CHECKED_TYPE_ID = "topic-type-id"
        private const val KEY_ADD_TEXT = "add-text"
        private const val KEY_ORDINAL = "ordinal"
        private const val KEY_DATE_MS = "date"
        private const val KEY_SELECTED_TYPE_ID = "type-id"
        private const val KEY_WAS_MODIFIED = "was-modified"

        fun factory(locale: Locale, subjectId: Int, topicId: Int) = viewModelFactory {
            initializer {
                TopicNameViewModel(
                    createSavedStateHandle(),
                    locale,
                    topicId,
                    subjectId,
                    CreateSubjectTopicUseCase(
                        App.module.topicRepository, CheckTopicNameAddTextUseCase(
                            GetAvailableTopicOrdinalUseCase(App.module.topicRepository)
                        )
                    ),
                    GetAvailableTopicOrdinalUseCase(App.module.topicRepository),
                    GetPrimaryTopicTypesUseCase(App.module.topicTypeRepository),
                    GetSecondaryTopicTypesUseCase(App.module.topicTypeRepository),
                    UpdateSubjectTopicUseCase(
                        App.module.topicRepository,
                        App.module.subjectRepository,
                        App.module.deadlineRepository
                    ),
                    GetTopicUseCase(App.module.topicRepository),
                    GetTopicTypeUseCase(App.module.topicTypeRepository),
                    ValidateTopicNameUseCase(
                        GetTopicByNameUseCase(App.module.topicRepository),
                        CheckTopicNameAddTextUseCase(
                            GetAvailableTopicOrdinalUseCase(App.module.topicRepository)
                        )
                    ),
                    CanTopicTypeBeReplacedUseCase()
                )
            }
        }
    }
}

sealed class TopicTypeItemUiState {
    data class Type(
        val typeId: Int,
        val name: UiText,
        val isSelected: Boolean,
        val isEnabled: Boolean,
        val onSelect: () -> Unit
    ) : TopicTypeItemUiState()

    data class Label(val text: UiText) : TopicTypeItemUiState()
}