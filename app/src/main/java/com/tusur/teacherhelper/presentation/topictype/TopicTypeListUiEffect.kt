package com.tusur.teacherhelper.presentation.topictype

import com.tusur.teacherhelper.presentation.core.base.TopLevelListUiEffect

sealed interface TopicTypeListUiEffect : TopLevelListUiEffect {
    data object FailedToDeleteBaseTopicType : TopicTypeListUiEffect
    data object FailedToDeleteUsedTopicType : TopicTypeListUiEffect
}