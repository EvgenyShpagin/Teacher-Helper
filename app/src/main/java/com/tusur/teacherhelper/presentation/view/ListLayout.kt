package com.tusur.teacherhelper.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.core.view.isGone
import com.tusur.teacherhelper.R


class ListLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyle, defStyleRes) {

    val hasVisibleItems get() = getChildAt(getLastVisibleItemPosition()) is ListItemView

    private val itemVerticalMargin = getDimen(R.dimen.group_list_item_vertical_margin)
    private val labelVerticalMargin = getDimen(R.dimen.group_list_label_vertical_margin)
    private val labelHorizontalMargin = getDimen(R.dimen.group_list_label_horizontal_margin)

    private var label: String? = null
        set(value) {
            if (field == value) return
            field = value
            if (value == null) {
                removeLabel()
            } else {
                showLabel()
            }
        }

    private var labelIsShown = false

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ListLayout, defStyle, defStyleRes)
            .apply {
                label = getString(R.styleable.ListLayout_label)
                recycle()
            }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var childrenHeight = 0
        var maxItemWidth = 0
        var childState = 0
        var verticalMargin = 0

        if (!hasVisibleItems) {
            setMeasuredDimension(0, 0)
            return
        }

        doForEachVisibleChild { child, _, isLast ->
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            childrenHeight += child.measuredHeight
            if (child is ListItemView) {
                maxItemWidth = maxItemWidth.coerceAtLeast(child.measuredWidth)
                if (!isLast) {
                    verticalMargin += itemVerticalMargin
                }
            } else {
                verticalMargin += labelVerticalMargin
            }
            childState = combineMeasuredStates(childState, child.measuredState)
        }

        val width = resolveSizeAndState(maxItemWidth, widthMeasureSpec, childState)
        val height = resolveSizeAndState(
            childrenHeight + verticalMargin,
            heightMeasureSpec,
            childState shl MEASURED_HEIGHT_STATE_SHIFT
        )
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (!hasVisibleItems) return

        if (changed) {
            updateItemsBackground()
        }

        val parentLeft = paddingLeft
        var currentTop = paddingTop

        doForEachVisibleChild { child, position, _ ->
            val width = child.measuredWidth
            val height = child.measuredHeight
            currentTop += getChildTopMargin(position)
            val leftMargin = getChildLeftMargin(position)
            child.layout(
                leftMargin + parentLeft,
                currentTop,
                width + leftMargin,
                currentTop + height
            )
            currentTop += height
        }
    }

    override fun shouldDelayChildPressedState() = false

    private fun showLabel() {
        val labelView = inflate(context, R.layout.group_list_label, null) as TextView
        labelView.text = label
        addView(labelView, 0)
        labelIsShown = true
    }

    private fun removeLabel() {
        val labelView = getChildAt(0) as TextView
        labelIsShown = false
        labelView.isGone = true
    }

    private fun getLastVisibleItemPosition(): Int {
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            if (!child.isGone) {
                return i
            }
        }
        return NO_POSITION
    }

    private fun doForEachVisibleChild(action: (child: View, position: Int, last: Boolean) -> Unit) {
        val lastVisiblePosition = getLastVisibleItemPosition()
        var notGoneIndex = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (!child.isGone) {
                action.invoke(child, notGoneIndex++, i == lastVisiblePosition)
            }
        }
    }

    private fun getChildLeftMargin(position: Int): Int {
        return if (position == 0 && label != null) {
            labelHorizontalMargin
        } else {
            0
        }
    }

    private fun getChildTopMargin(position: Int): Int {
        return if (position == 0) {
            0
        } else if (position == 1 && label != null) {
            labelVerticalMargin
        } else {
            itemVerticalMargin
        }
    }

    private fun updateItemsBackground() {
        var firstItemPassed = false
        doForEachVisibleChild { child, _, isLast ->
            if (child is ListItemView) {
                if (!firstItemPassed) {
                    if (isLast) {
                        child.position = ListItemView.ItemPosition.SINGLE
                    } else {
                        child.position = ListItemView.ItemPosition.FIRST
                    }
                    firstItemPassed = true
                } else if (isLast) {
                    child.position = ListItemView.ItemPosition.LAST
                } else {
                    child.position = ListItemView.ItemPosition.MIDDLE
                }
            }
        }
    }

    private fun getDimen(@DimenRes resId: Int): Int {
        return resources.getDimension(resId).toInt()
    }

    private companion object {
        const val NO_POSITION = -1
    }
}