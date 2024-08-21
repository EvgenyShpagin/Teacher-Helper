package com.tusur.teacherhelper.presentation.model

import android.content.Context
import androidx.annotation.StringRes

sealed class UiText {

    data class Dynamic(val value: String) : UiText() {
        override fun hashCode(): Int {
            return 31 * value.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Dynamic

            return value == other.value
        }
    }

    class Resource(@StringRes val resId: Int, vararg val args: Any) : UiText() {
        override fun hashCode(): Int {
            return 31 * resId + args.contentHashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Resource

            if (resId != other.resId) return false
            return args.contentEquals(other.args)
        }
    }

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