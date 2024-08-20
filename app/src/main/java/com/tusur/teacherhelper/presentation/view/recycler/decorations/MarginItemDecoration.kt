package com.tusur.teacherhelper.presentation.view.recycler.decorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(
    private val verticalSpace: Int = 0,
    private val horizontalSpace: Int = 0,
) : RecyclerView.ItemDecoration() {

    constructor(
        verticalSpace: Float = 0f,
        horizontalSpace: Float = 0f
    ) : this(
        verticalSpace = verticalSpace.toInt(),
        horizontalSpace = horizontalSpace.toInt()
    )

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = verticalSpace
            }
            left = horizontalSpace
            right = horizontalSpace
            bottom = verticalSpace
        }
    }
}