package com.tusur.teacherhelper.presentation.groups

import com.tusur.teacherhelper.presentation.core.base.TopLevelListUiEffect

sealed interface GroupsEffect : TopLevelListUiEffect {
    data object FailedToDeleteGroup : GroupsEffect
}