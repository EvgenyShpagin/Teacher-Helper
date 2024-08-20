package com.tusur.teacherhelper.presentation.model

import android.content.Context
import androidx.annotation.StringRes

sealed class UiText {

    data class Dynamic(val value: String) : UiText()
    class Resource(@StringRes val resId: Int, vararg val args: Any) : UiText()

    fun toString(context: Context): String {
        return when (this) {
            is Dynamic -> value
            is Resource -> context.getString(resId, *args)
        }
    }

    companion object {
        val empty = Dynamic("")
    }
}