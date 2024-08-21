package com.tusur.teacherhelper.presentation.subjectdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.model.Subject
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.usecase.DeleteSubjectGroupUseCase
import com.tusur.teacherhelper.domain.usecase.DoesTopicHaveClassDatetimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetClassDatetimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetGlobalTopicsUseCase
import com.tusur.teacherhelper.domain.usecase.GetLastClassDatetimeUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectByIdUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectGroupsUseCase
import com.tusur.teacherhelper.domain.usecase.GetSubjectTopicsUseCase
import com.tusur.teacherhelper.domain.usecase.SearchGlobalTopicUseCase
import com.tusur.teacherhelper.domain.usecase.SearchSubjectGroupUseCase
import com.tusur.teacherhelper.domain.usecase.SearchSubjectTopicUseCase
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.presentation.App
import com.tusur.teacherhelper.presentation.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale


class SubjectDetailsViewModel(
    val subjectId: Int,
    private val primaryLocale: Locale,
    private val getSubjectGroups: GetSubjectGroupsUseCase,
    private val getSubjectTopics: GetSubjectTopicsUseCase,
    private val getGlobalTopics: GetGlobalTopicsUseCase,
    getSubject: GetSubjectByIdUseCase,
    private val deleteSubjectGroup: DeleteSubjectGroupUseCase,
    private val searchGroup: SearchSubjectGroupUseCase,
    private val searchTopic: SearchSubjectTopicUseCase,
    private val searchGlobalTopic: SearchGlobalTopicUseCase,
    private val doesTopicHaveClassDatetime: DoesTopicHaveClassDatetimeUseCase,
    private val getLastClassDatetime: GetLastClassDatetimeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val checkedGroupIds = mutableListOf<Int>()
    private lateinit var subject: Subject

    init {
        viewModelScope.launch {
            subject = getSubject(subjectId)
            _uiState.update {
                it.copy(subjectName = UiText.Dynamic(subject.name))
            }

            launch {
                combine(
                    getSubjectTopics(subject = subject, withCancelled = true),
                    getGlobalTopics()
                ) { subjectTopics, globalTopics ->
                    mapAllTopicsToItemUi(subjectTopics, globalTopics)
                }.collect { uiItems ->
                    _uiState.update { state ->
                        state.copy(topicsUiState = uiItems)
                    }
                }
            }
            launch {
                _uiState.update { state ->
                    state.copy(
                        isFetching = false,
                        groupsUiState = getSubjectGroups(subjectId).first().map { group ->
                            group.toItemUiState()
                        }
                    )
                }
            }
        }
    }


    fun searchGroup(searchQuery: String) {
        _uiState.update { it.copy(isFetching = true) }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isFetching = false,
                    groupsUiState = searchGroup.invoke(subject, searchQuery)
                        .map { group -> group.toItemUiState() }
                )
            }
        }
    }

    fun searchTopic(searchQuery: String) {
        _uiState.update { it.copy(isFetching = true) }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isFetching = false,
                    topicsUiState = mapAllTopicsToItemUi(
                        searchTopic(subjectId, searchQuery),
                        searchGlobalTopic(searchQuery)
                    )
                )
            }
        }
    }

    fun stopDelete() {
        _uiState.update {
            it.copy(isDeleting = false)
        }
        checkedGroupIds.clear()
    }

    fun startDelete() {
        _uiState.update { it.copy(isDeleting = true) }
        checkedGroupIds.clear()
    }

    fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            deleteSubjectGroup(subjectId, groupId)
        }
    }

    fun setSelectedTab(fragment: Int) {
        _uiState.update { it.copy(currentFragmentIndex = fragment) }
    }

    private suspend fun mapAllTopicsToItemUi(
        subjectTopics: List<Topic>,
        globalTopics: List<Topic>
    ): List<TopicItemUiState> {
        val uiSubjectTopics = subjectTopics.map { it.toItemUiState(primaryLocale) }
        val uiGlobalTopicsLabel =
            TopicItemUiState.Label(UiText.Resource(R.string.global_topics_label))
        val uiGlobalTopics = globalTopics.map { it.toItemUiState(primaryLocale) }
        return if (uiGlobalTopics.isEmpty()) {
            uiSubjectTopics
        } else {
            uiSubjectTopics + uiGlobalTopicsLabel + uiGlobalTopics
        }
    }

    data class UiState(
        val isFetching: Boolean = true,
        val subjectName: UiText = UiText.empty,
        val groupsUiState: List<GroupItemUiState> = emptyList(),
        val topicsUiState: List<TopicItemUiState> = emptyList(),
        val isDeleting: Boolean = false,
        val currentFragmentIndex: Int = GROUPS_FRAGMENT_POSITION
    )

    private fun Group.toItemUiState() = GroupItemUiState(id, number)

    companion object {

        const val GROUPS_FRAGMENT_POSITION = 0
        const val TOPICS_FRAGMENT_POSITION = 1

        fun factory(subjectId: Int, locale: Locale) =
            viewModelFactory {
                initializer {
                    SubjectDetailsViewModel(
                        subjectId,
                        locale,
                        GetSubjectGroupsUseCase(
                            App.module.subjectGroupRepository,
                            App.module.groupRepository
                        ),
                        GetSubjectTopicsUseCase(App.module.topicRepository),
                        GetGlobalTopicsUseCase(App.module.topicRepository),
                        GetSubjectByIdUseCase(App.module.subjectRepository),
                        DeleteSubjectGroupUseCase(App.module.subjectGroupRepository),
                        SearchSubjectGroupUseCase(App.module.subjectGroupRepository),
                        SearchSubjectTopicUseCase(App.module.topicRepository),
                        SearchGlobalTopicUseCase(App.module.topicRepository),
                        DoesTopicHaveClassDatetimeUseCase(
                            GetClassDatetimeUseCase(App.module.classDateRepository)
                        ),
                        GetLastClassDatetimeUseCase(
                            GetClassDatetimeUseCase(App.module.classDateRepository)
                        ),
                    )
                }
            }
    }

    private suspend fun Topic.toItemUiState(locale: Locale): TopicItemUiState =
        TopicItemUiState.Topic(
            id = id,
            name = name.formatted(locale),
            lastClassDate = getLastClassDatetime(id)?.getDate(),
            isCancelled = isCancelled,
            isFinished = type.isAttendanceAcceptable
                    && doesTopicHaveClassDatetime(id)
                    && type.isAttendanceForOneClassOnly
        )
}

data class GroupItemUiState(
    val id: Int,
    val number: String
)

sealed class TopicItemUiState(val itemId: Int) {
    data class Topic(
        val id: Int,
        val name: String,
        val lastClassDate: Date?,
        val isFinished: Boolean,
        val isCancelled: Boolean
    ) : TopicItemUiState(id)

    data class Label(val text: UiText) : TopicItemUiState(-1)
}