package com.tusur.teacherhelper.presentation.subjectgroups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.subjectdetails.SimpleGroupItemUiState


class CheckableGroupAdapter :
    ListAdapter<SimpleGroupItemUiState, CheckableGroupAdapter.GroupViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_checkable_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group = group)
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView = itemView.findViewById<TextView>(R.id.group_text_view)
        private val checkbox = itemView.findViewById<CheckBox>(R.id.group_checkbox)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                val group = getItem(position)
                group.onCheck()
            }
        }

        fun bind(group: SimpleGroupItemUiState) {
            textView.text = group.number
            checkbox.isChecked = group.isChecked
        }
    }

    private companion object {
        class DiffUtilCallback : DiffUtil.ItemCallback<SimpleGroupItemUiState>() {
            override fun areItemsTheSame(
                oldItem: SimpleGroupItemUiState, newItem: SimpleGroupItemUiState
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SimpleGroupItemUiState, newItem: SimpleGroupItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
