package com.tusur.teacherhelper.presentation.topicperformance

import android.widget.TextView
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.view.recycler.BaseDeletableAdapter


class DeletableDateAdapter(
    onClickListener: Listener<DateItemUiState>
) : BaseDeletableAdapter<DateItemUiState>(
    listener = onClickListener,
    idSelector = { it.dateMillis.hashCode() },
    firstItemLayoutResId = R.layout.group_list_first_item_text_and_next_arrow,
    lastItemLayoutResId = R.layout.group_list_last_item_text_and_next_arrow,
    middleItemLayoutResId = R.layout.group_list_middle_item_text_and_next_arrow,
    singleItemLayoutResId = R.layout.group_list_single_item_text_and_next_arrow,
    deletableItemLayoutResId = R.layout.list_item_text_and_delete_bt
) {
    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        val textView = holder.itemView.findViewById<TextView>(R.id.text_view)
        val date = getItem(position)
        textView.text = date.dateText
    }
}