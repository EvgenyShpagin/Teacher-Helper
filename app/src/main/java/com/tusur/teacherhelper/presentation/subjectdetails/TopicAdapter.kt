package com.tusur.teacherhelper.presentation.subjectdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.core.util.formatted


class TopicAdapter(
    private val onClickListener: OnClickListener,
) : ListAdapter<TopicItemUiState, ViewHolder>(itemCallback) {

    override fun getItemViewType(position: Int): Int {
        return getItem(position).let { uiItem ->
            when (uiItem) {
                is TopicItemUiState.Topic ->
                    if (shouldTopicItemBeTwoLine(uiItem)) {
                        TWO_LINE_TOPIC_VIEW_TYPE
                    } else {
                        ONE_LINE_TOPIC_VIEW_TYPE
                    }

                else -> LABEL_VIEW_TYPE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        fun inflate(@LayoutRes res: Int) = inflater.inflate(res, parent, false)

        return when (viewType) {
            ONE_LINE_TOPIC_VIEW_TYPE -> OneLineTopicViewHolder(inflate(R.layout.list_item_topic_one_line))
            TWO_LINE_TOPIC_VIEW_TYPE -> TwoLineTopicViewHolder(inflate(R.layout.list_item_topic_two_line))
            LABEL_VIEW_TYPE -> LabelViewHolder(inflate(R.layout.list_item_label))

            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is TwoLineTopicViewHolder -> holder.bind(item as TopicItemUiState.Topic)
            is OneLineTopicViewHolder -> holder.bind(item as TopicItemUiState.Topic)
            is LabelViewHolder -> holder.bind(item as TopicItemUiState.Label)
        }
    }

    inner class LabelViewHolder(itemView: View) : ViewHolder(itemView) {
        private val labelTextView = itemView as TextView

        init {
            itemView.setOnClickListener {
                onClickListener.onClick(itemView, getItem(adapterPosition))
            }
        }

        fun bind(item: TopicItemUiState.Label) {
            labelTextView.text = item.text.toString(itemView.context)
        }
    }

    open inner class OneLineTopicViewHolder(itemView: View) : ViewHolder(itemView) {
        protected val topicTextView: TextView = itemView.findViewById(R.id.headline)
        private val trailingImageView: ImageView = itemView.findViewById(R.id.trailing_icon)

        init {
            itemView.setOnClickListener {
                val topic = getItem(adapterPosition) as TopicItemUiState.Topic
                onClickListener.onClick(itemView, topic)
            }
        }

        open fun bind(topic: TopicItemUiState.Topic) {
            topicTextView.text = topic.name
            trailingImageView.isVisible = topic.isFinished
            ViewCompat.setTransitionName(itemView, "transition-${topic.itemId}")
        }
    }

    inner class TwoLineTopicViewHolder(itemView: View) : OneLineTopicViewHolder(itemView) {
        private val supportTextView: TextView = itemView.findViewById(R.id.support_text)

        override fun bind(topic: TopicItemUiState.Topic) {
            super.bind(topic)
            // Content for 2nd line
            assert(topic.isCancelled || topic.lastClassDate != null)
            if (topic.isCancelled) {
                disable()
                supportTextView.text = itemView.context.getText(R.string.cancelled_topic_label)
            } else {
                enable()
                supportTextView.text = itemView.context.getString(
                    R.string.class_date_label, topic.lastClassDate!!.formatted()
                )
            }
        }

        private fun enable() {
            topicTextView.alpha = 1.0f
            supportTextView.alpha = 1.0f
            itemView.isEnabled = true
        }

        private fun disable() {
            topicTextView.alpha = 0.6f
            supportTextView.alpha = 0.6f
            itemView.isEnabled = false
        }
    }

    fun interface OnClickListener {
        fun onClick(view: View, item: TopicItemUiState)
    }

    private fun shouldTopicItemBeTwoLine(topic: TopicItemUiState.Topic) =
        topic.isCancelled || topic.lastClassDate != null


    private companion object {
        const val ONE_LINE_TOPIC_VIEW_TYPE = 0
        const val TWO_LINE_TOPIC_VIEW_TYPE = 1
        const val LABEL_VIEW_TYPE = 2

        private val itemCallback = object : DiffUtil.ItemCallback<TopicItemUiState>() {
            override fun areItemsTheSame(
                oldItem: TopicItemUiState,
                newItem: TopicItemUiState
            ): Boolean {
                return oldItem.itemId == newItem.itemId
            }

            override fun areContentsTheSame(
                oldItem: TopicItemUiState,
                newItem: TopicItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}