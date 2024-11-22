package com.tusur.teacherhelper.presentation.performance

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseAdapter


class TopicSummaryAdapter : BaseAdapter<TopicResultUiItem>(
    listener = null,
    idSelector = { it.topicId },
    itemLayoutResId = R.layout.list_item_topic_results
) {

    inner class ItemViewHolder(itemView: View) : DefaultViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.text_view)
        private val trailingTextView: TextView = itemView.findViewById(R.id.trailing_text_view)

        fun bind(topicResult: TopicResultUiItem) {
            textView.text = topicResult.topicName.toString(itemView.context)
            trailingTextView.text = topicResult.results.toString(itemView.context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        return ItemViewHolder(parent.inflate(res = getLayoutResFromViewType(viewType)))
    }

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        holder as ItemViewHolder
        holder.bind(topicResult = getItem(position))
    }
}