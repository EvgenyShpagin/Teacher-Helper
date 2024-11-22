package com.tusur.teacherhelper.presentation.topic

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.core.view.ListItemView
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseAdapter


class ClassTimeAdapter : BaseAdapter<ClassTimeItemUiState>(
    listener = null,
    idSelector = { item -> item.id },
    itemLayoutResId = R.layout.list_item_class_time_range
) {
    private var _cachedDrawable: Drawable? = null


    inner class ClassTimeViewHolder(itemView: View) : DefaultViewHolder(itemView) {
        private val listItemView = itemView as ListItemView

        init {
            itemView.setOnClickListener {
                val item = getItem(adapterPosition)
                item.onClick()
            }
        }

        fun bind(itemUiState: ClassTimeItemUiState) {
            listItemView.title = itemUiState.classTimeRange.toString(itemView.context)
            listItemView.setTrailingDrawable(
                drawable = if (itemUiState.hasPerformance) {
                    getHasPerformanceDrawable(itemView.context)
                } else {
                    null
                }
            )

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        return ClassTimeViewHolder(parent.inflate(R.layout.list_item_class_time_range))
    }

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        holder as ClassTimeViewHolder
        holder.bind(itemUiState = getItem(position))
    }

    private fun getHasPerformanceDrawable(context: Context): Drawable {
        return _cachedDrawable ?: ContextCompat.getDrawable(
            context, R.drawable.calendar_check_24
        )!!.also { _cachedDrawable = it }
    }
}