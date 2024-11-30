package com.tusur.teacherhelper.presentation.performance

import android.view.ViewGroup
import android.widget.TextView
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseAdapter

class StudentSummaryPerformanceProgressAdapter(
    onClick: (TopicTypeUiItem) -> Unit
) : BaseAdapter<TopicTypeUiItem>(
    listener = object : BaseAdapter.OnClickListener<TopicTypeUiItem> {
        override fun onClick(item: TopicTypeUiItem) {
            onClick.invoke(item)
        }
    },
    idSelector = { it.typeId },
    firstItemLayoutResId = R.layout.group_list_first_item_topic_type_progress,
    middleItemLayoutResId = R.layout.group_list_middle_item_topic_type_progress,
    lastItemLayoutResId = R.layout.group_list_last_item_topic_type_progress,
    singleItemLayoutResId = R.layout.group_list_single_item_topic_type_progress,
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseAdapter<TopicTypeUiItem>.DefaultViewHolder {
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(
        holder: BaseAdapter<TopicTypeUiItem>.DefaultViewHolder,
        position: Int
    ) {
        val context = holder.itemView.context
        val typeUiItem = getItem(position)
        val typeNameTextView = holder.itemView.findViewById<TextView>(R.id.text_view)
        val typeProgressTextView = holder.itemView.findViewById<TextView>(R.id.trailing_text_view)

        typeNameTextView.text = typeUiItem.name.toString(context)
        typeProgressTextView.text = typeUiItem.progress.toString(context)
    }
}