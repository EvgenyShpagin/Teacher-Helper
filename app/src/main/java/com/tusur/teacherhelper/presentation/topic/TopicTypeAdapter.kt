package com.tusur.teacherhelper.presentation.topic

import android.widget.TextView
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.presentation.core.view.ListItemView
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseAdapter
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseLabelledAdapter

class TopicTypeAdapter : BaseLabelledAdapter<TopicTypeItemUiState>(
    listener = object : BaseAdapter.OnClickListener<TopicTypeItemUiState> {
        override fun onClick(item: TopicTypeItemUiState) {
            item as TopicTypeItemUiState.Type
            item.onSelect()
        }
    },
    idSelector = {
        if (it is TopicTypeItemUiState.Type) {
            it.typeId
        } else {
            NO_ID
        }
    },
    labelPredicate = { it is TopicTypeItemUiState.Label },
    itemLayoutResId = R.layout.list_item_checkable_topic_type,
) {

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        val item = getItem(position)
        val view = holder.itemView
        when (holder) {
            is LabelViewHolder -> {
                item as TopicTypeItemUiState.Label
                view as TextView
                view.text = item.text.toString(view.context)
            }

            else -> {
                item as TopicTypeItemUiState.Type
                view as ListItemView
                view.title = item.name.toString(view.context)
                view.isChecked = item.isSelected
                view.isEnabled = item.isEnabled
                setupItemBackground(view, position)
            }
        }
    }

    private fun setupItemBackground(itemView: ListItemView, itemPosition: Int) {
        itemView.position = when (itemCount) {
            0, 1 -> return
            else -> {
                when (itemPosition) {
                    1 -> if (itemCount == 2) {
                        ListItemView.ItemPosition.SINGLE
                    } else {
                        ListItemView.ItemPosition.FIRST
                    }

                    itemCount - 1 -> ListItemView.ItemPosition.LAST
                    else -> ListItemView.ItemPosition.MIDDLE
                }
            }
        }
    }
}