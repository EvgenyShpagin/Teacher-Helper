package com.tusur.teacherhelper.presentation.globaltopic

import com.tusur.teacherhelper.presentation.core.base.TopLevelListUiEffect

sealed interface GlobalTopicListEffect : TopLevelListUiEffect {
    data object FailedToDeleteDeadline : GlobalTopicListEffect
}