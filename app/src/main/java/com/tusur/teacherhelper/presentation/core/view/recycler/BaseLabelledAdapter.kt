package com.tusur.teacherhelper.presentation.core.view.recycler

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.view.updateLayoutParams
import com.tusur.teacherhelper.R


abstract class BaseLabelledAdapter<T>(
    private val listener: BaseAdapter.OnClickListener<T>?,
    private val idSelector: (item: T) -> Int,
    private val labelPredicate: (item: T) -> Boolean,
    @LayoutRes private val firstItemLayoutResId: Int,
    @LayoutRes private val lastItemLayoutResId: Int,
    @LayoutRes private val middleItemLayoutResId: Int,
    @LayoutRes private val singleItemLayoutResId: Int,
) : BaseAdapter<T>(
    listener = listener,
    idSelector = idSelector,
    firstItemLayoutResId = firstItemLayoutResId,
    lastItemLayoutResId = lastItemLayoutResId,
    middleItemLayoutResId = middleItemLayoutResId,
    singleItemLayoutResId = singleItemLayoutResId
) {
    constructor(
        listener: BaseAdapter.OnClickListener<T>?,
        idSelector: (item: T) -> Int,
        labelPredicate: (item: T) -> Boolean,
        @LayoutRes itemLayoutResId: Int
    ) : this(
        listener = listener,
        idSelector = idSelector,
        labelPredicate = labelPredicate,
        firstItemLayoutResId = itemLayoutResId,
        lastItemLayoutResId = itemLayoutResId,
        middleItemLayoutResId = itemLayoutResId,
        singleItemLayoutResId = itemLayoutResId,
    )

    @LayoutRes
    private val labelItemLayoutResId = R.layout.group_list_label

    private var labelPositions = arrayListOf<Int>()

    override fun submitList(list: List<T>?) {
        labelPositions.clear()
        list?.forEachIndexed { index, item ->
            if (labelPredicate(item)) {
                labelPositions.add(index)
            }
        }
        super.submitList(list)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position in labelPositions) {
            LABEL_ITEM
        } else {
            if (itemCount == 1) {
                DEFAULT_ITEM_SINGLE
            } else {
                val prevLabelPosition = labelPositions.indexOfFirst { position > it }
                val nextLabelPosition = labelPositions.indexOfLast { position < it }
                val lastIndex = nextLabelPosition.let { if (it == -1) itemCount - 1 else it }
                when (position) {
                    prevLabelPosition + 1 -> DEFAULT_ITEM_FIRST
                    in prevLabelPosition..lastIndex -> DEFAULT_ITEM_MIDDLE
                    else -> DEFAULT_ITEM_LAST
                }
            }
        }
    }

    open inner class LabelViewHolder(itemView: View) : DefaultViewHolder(itemView) {
        private val labelTextView = itemView.findViewById<TextView>(R.id.label)

        init {
            itemView.setOnClickListener(null)
            setMargins()
            labelTextView.setOnClickListener {
                if (listener == null) return@setOnClickListener
                if (listener is OnClickListener) {
                    val item = getItem(adapterPosition)
                    listener.onLabelClick(item)
                } // else do nothing
            }
        }

        private fun setMargins() {
            val res = itemView.resources
            labelTextView.updateLayoutParams<MarginLayoutParams> {
                marginStart = res.getDimension(R.dimen.group_list_label_horizontal_margin).toInt()
                bottomMargin = res.getDimension(R.dimen.group_list_label_vertical_margin).toInt() -
                        res.getDimension(R.dimen.group_list_item_vertical_margin).toInt()
                // margin of group_list_item_vertical_margin is already added
                // so it is required to set margin of rest size
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        val view = parent.inflate(getLayoutResFromViewType(viewType))
        return if (viewType == LABEL_ITEM) {
            LabelViewHolder(view)
        } else {
            DefaultViewHolder(view)
        }
    }

    override fun getLayoutResFromViewType(viewType: Int): Int {
        return if (viewType == LABEL_ITEM) {
            labelItemLayoutResId
        } else {
            super.getLayoutResFromViewType(viewType)
        }
    }

    interface OnClickListener<T> : BaseAdapter.OnClickListener<T> {
        override fun onClick(item: T)
        fun onLabelClick(item: T)
    }

    companion object {
        const val LABEL_ITEM = 1
    }
}