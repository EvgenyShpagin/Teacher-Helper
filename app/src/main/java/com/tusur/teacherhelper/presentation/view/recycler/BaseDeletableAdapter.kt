package com.tusur.teacherhelper.presentation.view.recycler

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.LayoutRes
import com.tusur.teacherhelper.R


abstract class BaseDeletableAdapter<T>(
    private val listener: Listener<T>?,
    private val idSelector: (item: T) -> Int,
    @LayoutRes private val firstItemLayoutResId: Int,
    @LayoutRes private val lastItemLayoutResId: Int,
    @LayoutRes private val middleItemLayoutResId: Int,
    @LayoutRes private val singleItemLayoutResId: Int,
    @LayoutRes private val deletableItemLayoutResId: Int
) : BaseAdapter<T>(
    listener = listener,
    idSelector = idSelector,
    firstItemLayoutResId = firstItemLayoutResId,
    lastItemLayoutResId = lastItemLayoutResId,
    middleItemLayoutResId = middleItemLayoutResId,
    singleItemLayoutResId = singleItemLayoutResId
) {

    constructor(
        listener: Listener<T>,
        idSelector: (item: T) -> Int,
        @LayoutRes itemLayoutResId: Int,
        @LayoutRes deletableItemLayoutResId: Int
    ) : this(
        listener = listener,
        idSelector = idSelector,
        firstItemLayoutResId = itemLayoutResId,
        lastItemLayoutResId = itemLayoutResId,
        middleItemLayoutResId = itemLayoutResId,
        singleItemLayoutResId = itemLayoutResId,
        deletableItemLayoutResId = deletableItemLayoutResId
    )

    var isDeleting = false
        set(value) {
            if (field == value) return
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    override fun getItemViewType(position: Int): Int {
        return if (isDeleting) {
            DELETABLE_ITEM
        } else {
            super.getItemViewType(position)
        }
    }

    open inner class DeletableViewHolder(itemView: View) : DefaultViewHolder(itemView) {
        private val deleteButton = itemView.findViewById<ImageButton>(R.id.delete_button)

        init {
            itemView.setOnClickListener(null)
            deleteButton.setOnClickListener {
                val item = getItem(adapterPosition)
                listener?.onDelete(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        val view = parent.inflate(getLayoutResFromViewType(viewType))
        return if (viewType == DELETABLE_ITEM) {
            DeletableViewHolder(view)
        } else {
            DefaultViewHolder(view)
        }
    }

    override fun getLayoutResFromViewType(viewType: Int): Int {
        return if (viewType == DELETABLE_ITEM) {
            deletableItemLayoutResId
        } else {
            super.getLayoutResFromViewType(viewType)
        }
    }

    interface Listener<T> : OnClickListener<T> {
        override fun onClick(item: T)
        fun onDelete(item: T)
    }

    companion object {
        const val DELETABLE_ITEM = 1
    }
}