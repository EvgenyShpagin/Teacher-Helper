package com.tusur.teacherhelper.presentation.groups

import android.widget.TextView
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseDeletableAdapter

class GroupAdapter(
    onClickListener: Listener<GroupItemUiState>
) : BaseDeletableAdapter<GroupItemUiState>(
    listener = onClickListener,
    idSelector = { it.id },
    itemLayoutResId = R.layout.list_item_text_and_next_arrow,
    deletableItemLayoutResId = R.layout.list_item_text_and_delete_bt
) {
    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        val textView = holder.itemView.findViewById<TextView>(R.id.text_view)
        textView.text = getItem(position).number
    }
}