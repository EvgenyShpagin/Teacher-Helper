package com.tusur.teacherhelper.presentation.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.tusur.teacherhelper.presentation.util.withOtherColor

data class Icon(
    @DrawableRes val iconRes: Int,
    @ColorRes val colorRes: Int? = null,
    @AttrRes val colorAttrRes: Int? = null
) {
    fun toDrawable(context: Context): Drawable {
        val drawable = ContextCompat.getDrawable(context, iconRes)!!
        val color = parseColor(context)
        return color?.let { drawable.withOtherColor(it) } ?: drawable
    }

    @ColorInt
    private fun parseColor(context: Context): Int? {
        return colorRes?.let { context.getColor(colorRes) }
            ?: colorAttrRes?.let { MaterialColors.getColorOrNull(context, it) }
    }
}
