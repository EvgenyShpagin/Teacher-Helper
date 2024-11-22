package com.tusur.teacherhelper.presentation.core.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.core.util.withOtherColor


class InfoView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textAppearanceBodyMedium
) : MaterialTextView(context, attributeSet, defStyleAttr) {
    init {
        val color = MaterialColors.getColor(
            this, com.google.android.material.R.attr.colorOnSurfaceVariant
        )
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_info_24)!!
            .withOtherColor(color)
        setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
        val defaultMargin = resources.getDimension(R.dimen.default_content_margin).toInt()
        compoundDrawablePadding = defaultMargin
        setTextColor(color)
    }
}