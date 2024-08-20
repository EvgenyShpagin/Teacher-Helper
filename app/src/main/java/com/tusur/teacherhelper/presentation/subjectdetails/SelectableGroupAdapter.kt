package com.tusur.teacherhelper.presentation.subjectdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tusur.teacherhelper.R


class SelectableGroupAdapter :
    ListAdapter<SimpleGroupItemUiState, SelectableGroupAdapter.GroupViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_selectable_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group = group)
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView = itemView as TextView

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                val group = getItem(position)
                group.onCheck()
            }
        }

        fun bind(group: SimpleGroupItemUiState) {
            textView.isActivated = group.isChecked
            textView.text = group.number
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
