package com.tusur.teacherhelper.presentation.core.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.material.R.attr
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.color.MaterialColors
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.radiobutton.MaterialRadioButton
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.R.styleable.ListItemView_isChecked
import com.tusur.teacherhelper.R.styleable.ListItemView_leadingComponent
import com.tusur.teacherhelper.R.styleable.ListItemView_leadingDrawable
import com.tusur.teacherhelper.R.styleable.ListItemView_position
import com.tusur.teacherhelper.R.styleable.ListItemView_title
import com.tusur.teacherhelper.R.styleable.ListItemView_trailingComponent
import com.tusur.teacherhelper.R.styleable.ListItemView_trailingDrawable
import com.tusur.teacherhelper.R.styleable.ListItemView_trailingSupportText
import com.tusur.teacherhelper.presentation.core.util.withOtherColor


class ListItemView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {

    private val view = LayoutInflater.from(context)
        .inflate(R.layout.group_list_item_view, this, true)

    private val radioButton: MaterialRadioButton = view.findViewById(R.id.radio_button)
    private val checkbox: MaterialCheckBox = view.findViewById(R.id.checkbox)
    private val trailingTextView: TextView = view.findViewById(R.id.trailing_text_view)
    private val switch: MaterialSwitch = view.findViewById(R.id.switch_view)

    private var isError = false

    private val defaultTextColor = MaterialColors.getColor(this, attr.colorOnSurface)
    private val textColorVariant = MaterialColors.getColor(this, attr.colorOnSurfaceVariant)
    private val errorTextColor = MaterialColors.getColor(this, attr.colorError)
    private val disabledTextColor = ColorUtils.setAlphaComponent(defaultTextColor, 0xAA)
    private val disabledVariantTextColor = ColorUtils.setAlphaComponent(defaultTextColor, 0xAA)
    private val disabledErrorTextColor = ColorUtils.setAlphaComponent(errorTextColor, 0xAA)

    private var keepOriginLeadingIconColor = false
    private var keepOriginTrailingIconColor = false

    private val radioButtonDrawable = radioButton.buttonDrawable
    private val radioButtonEmptyDrawable = StateListDrawable()

    private val margin8dp = resources.getDimension(R.dimen.small_content_margin)

    private val checkboxMargin = (margin8dp / 2f).toInt()
    private val radioButtonMargin = (margin8dp * 1.5).toInt()
    private val defaultMargin = resources.getDimension(R.dimen.default_content_margin).toInt()

    private val primaryColor
        get() = when {
            isError && !isEnabled -> disabledErrorTextColor
            isError -> errorTextColor
            !isEnabled -> disabledTextColor
            else -> defaultTextColor
        }

    private val supportTextColor
        get() = when {
            isError && !isEnabled -> disabledErrorTextColor
            isError -> errorTextColor
            !isEnabled -> disabledVariantTextColor
            else -> textColorVariant
        }

    var leadingComponent = Component.RADIO_BUTTON
        set(value) {
            if (field == value) return
            val prevComponent = leadingComponent
            field = value
            when (prevComponent) {
                Component.CHECKBOX -> checkbox.isVisible = false
                Component.RADIO_BUTTON -> removeRadioButton()
                Component.ICON -> radioButton.setStartDrawable(null)
                else -> {}
            }
            when (value) {
                Component.CHECKBOX -> {
                    checkbox.updateLayoutParams { marginStart = checkboxMargin }
                    checkbox.isVisible = true
                    radioButton.updateLayoutParams { marginStart = checkboxMargin }
                }

                Component.RADIO_BUTTON -> {
                    radioButton.isEnabled = true
                    radioButton.buttonDrawable = radioButtonDrawable
                    radioButton.setPaddingRelative(radioButtonMargin, 0, 0, 0)
                    radioButton.updateLayoutParams { marginStart = radioButtonMargin }
                }

                Component.NOTHING -> radioButton.updateLayoutParams { marginStart = defaultMargin }
                Component.ICON -> radioButton.updateLayoutParams { marginStart = defaultMargin }
                else -> {}
            }
        }

    var position: ItemPosition? = null
        set(value) {
            field = value
            setBackground(field)
        }

    var isChecked: Boolean = false
        set(value) {
            field = value
            if (leadingComponent == Component.CHECKBOX) {
                checkbox.isChecked = value
            } else if (leadingComponent == Component.RADIO_BUTTON) {
                radioButton.isChecked = value
            } else if (trailingComponent == Component.SWITCH) {
                switch.isChecked = value
            }
        }

    var trailingComponent: Component = Component.NOTHING
        set(value) {
            if (field == value) return
            when (field) {
                Component.ICON -> radioButton.setEndDrawable(null)
                Component.SUPPORT_TEXT -> trailingTextView.isVisible = false
                Component.SWITCH -> switch.isVisible = false
                else -> {}
            }
            field = value
            when (value) {
                Component.NOTHING -> radioButton.setEndDrawable(null)
                Component.SUPPORT_TEXT -> trailingTextView.isVisible = true
                Component.SWITCH -> switch.isVisible = true
                else -> return
            }
        }

    var title: CharSequence
        get() = radioButton.text
        set(value) {
            radioButton.text = value
        }

    var trailingSupportText: CharSequence
        get() = trailingTextView.text
        set(value) {
            if (value.isNotBlank()) {
                trailingComponent = Component.SUPPORT_TEXT
            }
            if (trailingComponent == Component.SUPPORT_TEXT) {
                trailingTextView.text = value
            }
        }

    init {
        attributeSet?.let { initAttrs(it) }
        isClickable = true
        isFocusable = true
    }

    override fun setEnabled(enabled: Boolean) {
        if (isEnabled == enabled) return
        super.setEnabled(enabled)
        updateColors()
        radioButton.isEnabled = enabled
    }

    fun setTrailingDrawable(drawable: Drawable?, keepOriginColor: Boolean = false) {
        keepOriginTrailingIconColor = keepOriginColor
        setTrailingDrawableInternal(drawable)
        updateColors()
    }

    fun setLeadingDrawable(drawable: Drawable?, keepOriginColor: Boolean = false) {
        keepOriginLeadingIconColor = keepOriginColor
        setLeadingDrawableInternal(drawable)
        updateColors()
    }


    private fun initAttrs(attributeSet: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.ListItemView,
            0, 0
        ).apply {
            try {
                getBoolean(R.styleable.ListItemView_isErrorColor, false).also {
                    isError = it
                }
                getBoolean(R.styleable.ListItemView_isEnabled, true).also {
                    isEnabled = it
                }
                getInteger(ListItemView_position, 0).also {
                    val position = ItemPosition.entries[it]
                    setBackground(position)
                }
                getInteger(ListItemView_leadingComponent, leadingComponent.ordinal).also {
                    val component = Component.entries[it]
                    leadingComponent = component
                }
                getInteger(ListItemView_trailingComponent, trailingComponent.ordinal).also {
                    val component = Component.entries[it]
                    trailingComponent = component
                }
                getBoolean(ListItemView_isChecked, false).also {
                    isChecked = it
                }
                getDrawable(ListItemView_leadingDrawable).also {
                    setLeadingDrawableInternal(it)
                }
                getDrawable(ListItemView_trailingDrawable).also {
                    setTrailingDrawableInternal(it)
                }
                getString(ListItemView_trailingSupportText).also {
                    trailingSupportText = it ?: ""
                }
                title = getString(ListItemView_title) ?: ""
                updateColors()
            } finally {
                recycle()
            }
        }
    }

    private fun setLeadingDrawableInternal(drawable: Drawable?) {
        if (drawable != null) {
            leadingComponent = Component.ICON
        }
        if (leadingComponent == Component.ICON) {
            radioButton.setStartDrawable(drawable)
        }
    }

    private fun setTrailingDrawableInternal(drawable: Drawable?) {
        if (drawable != null) {
            trailingComponent = Component.ICON
        }
        if (trailingComponent == Component.ICON) {
            radioButton.setEndDrawable(drawable)
        }
    }

    private fun updateColors() {
        radioButton.setTextColor(primaryColor)
        if (trailingComponent == Component.SUPPORT_TEXT) {
            trailingTextView.setTextColor(supportTextColor)
        }
        val setDrawables = radioButton.compoundDrawablesRelative
        radioButton.setStartAndEndDrawables(
            startDrawable = if (keepOriginLeadingIconColor) {
                setDrawables[0]
            } else {
                setDrawables[0]?.withOtherColor(primaryColor)
            },
            endDrawable = if (keepOriginTrailingIconColor) {
                setDrawables[2]
            } else {
                setDrawables[2]?.withOtherColor(primaryColor)
            }
        )
    }

    private fun TextView.setStartAndEndDrawables(startDrawable: Drawable?, endDrawable: Drawable?) {
        setCompoundDrawablesRelativeWithIntrinsicBounds(startDrawable, null, endDrawable, null)
    }

    private fun TextView.setStartDrawable(drawable: Drawable?) {
        val endDrawable = if (trailingComponent == Component.ICON) {
            radioButton.compoundDrawablesRelative[2]
        } else {
            null
        }
        setStartAndEndDrawables(startDrawable = drawable, endDrawable = endDrawable)
    }

    private fun TextView.setEndDrawable(drawable: Drawable?) {
        val startDrawable = if (leadingComponent == Component.ICON) {
            radioButton.compoundDrawablesRelative[0]
        } else {
            null
        }
        setStartAndEndDrawables(startDrawable = startDrawable, endDrawable = drawable)
    }

    private fun removeRadioButton() {
        radioButton.isEnabled = false
        radioButton.buttonDrawable = radioButtonEmptyDrawable
        radioButton.setPaddingRelative(0, 0, 0, 0)
    }

    private fun setBackground(position: ItemPosition?) {
        when (position) {
            ItemPosition.FIRST -> setBackgroundResource(R.drawable.group_list_first_item_ripple)
            ItemPosition.MIDDLE -> setBackgroundResource(R.drawable.group_list_middle_item_ripple)
            ItemPosition.LAST -> setBackgroundResource(R.drawable.group_list_last_item_ripple)
            ItemPosition.SINGLE -> setBackgroundResource(R.drawable.group_list_single_item_ripple)
            null -> setBackgroundResource(0)
        }
    }

    private inline fun View.updateLayoutParams(block: MarginLayoutParams.() -> Unit) {
        updateLayoutParams<LayoutParams>(block)
    }

    enum class Component {
        CHECKBOX,
        RADIO_BUTTON,
        ICON,
        NOTHING,
        SUPPORT_TEXT,
        SWITCH
    }

    enum class ItemPosition {
        FIRST,
        MIDDLE,
        LAST,
        SINGLE
    }
}