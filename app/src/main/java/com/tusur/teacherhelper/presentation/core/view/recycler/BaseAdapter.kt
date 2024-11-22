package com.tusur.teacherhelper.presentation.core.view.recycler

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder


abstract class BaseAdapter<T>(
    private val listener: OnClickListener<T>?,
    private val idSelector: (item: T) -> Int,
    @LayoutRes private val firstItemLayoutResId: Int,
    @LayoutRes private val lastItemLayoutResId: Int,
    @LayoutRes private val middleItemLayoutResId: Int,
    @LayoutRes private val singleItemLayoutResId: Int
) : ListAdapter<T, BaseAdapter<T>.DefaultViewHolder>(createDiffCallback(idSelector)) {

    constructor(
        listener: OnClickListener<T>?,
        idSelector: (item: T) -> Int,
        @LayoutRes itemLayoutResId: Int
    ) : this(
        listener = listener,
        idSelector = idSelector,
        firstItemLayoutResId = itemLayoutResId,
        lastItemLayoutResId = itemLayoutResId,
        middleItemLayoutResId = itemLayoutResId,
        singleItemLayoutResId = itemLayoutResId
    )

    override fun getItemViewType(position: Int): Int {
        return if (itemCount == 1) {
            DEFAULT_ITEM_SINGLE
        } else {
            if (position == 0) {
                DEFAULT_ITEM_FIRST
            } else if (position != itemCount - 1) {
                DEFAULT_ITEM_MIDDLE
            } else {
                DEFAULT_ITEM_LAST
            }
        }
    }

    open inner class DefaultViewHolder(itemView: View) : ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val item = getItem(adapterPosition)
                listener?.onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        return DefaultViewHolder(parent.inflate(res = getLayoutResFromViewType(viewType)))
    }

    @LayoutRes
    protected open fun getLayoutResFromViewType(viewType: Int): Int {
        return when (viewType) {
            DEFAULT_ITEM_SINGLE -> singleItemLayoutResId
            DEFAULT_ITEM_FIRST -> firstItemLayoutResId
            DEFAULT_ITEM_MIDDLE -> middleItemLayoutResId
            else -> lastItemLayoutResId
        }
    }

    protected fun ViewGroup.inflate(@LayoutRes res: Int): View {
        return LayoutInflater.from(context).inflate(res, this, false)
    }

    interface OnClickListener<T> {
        fun onClick(item: T)
    }

    companion object {
        const val DEFAULT_ITEM_SINGLE = 2
        const val DEFAULT_ITEM_MIDDLE = 3
        const val DEFAULT_ITEM_FIRST = 4
        const val DEFAULT_ITEM_LAST = 5

        private fun <T> createDiffCallback(idSelector: (item: T) -> Int): DiffUtil.ItemCallback<T> {
            return object : DiffUtil.ItemCallback<T>() {
                override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
                    return idSelector(oldItem) == idSelector(newItem)
                }

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
                    return oldItem == newItem
                }
            }
        }
    }
}