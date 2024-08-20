package com.tusur.teacherhelper.presentation.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tusur.teacherhelper.R


class EditableDateAdapter(
    private val onClickListener: OnClickListener
) : ListAdapter<DatetimeItemUiState, EditableDateAdapter.DateViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_text_and_edit_bt, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = getItem(position)
        holder.bind(classDate = date)
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView = itemView.findViewById<TextView>(R.id.text_view)
        private val editButton = itemView.findViewById<ImageButton>(R.id.edit_button)

        init {
            editButton.setOnClickListener {
                val position = adapterPosition
                onClickListener.onClick(datetimeItemUiState = getItem(position))
            }
        }

        fun bind(classDate: DatetimeItemUiState) {
            textView.text = classDate.datetimeText.toString(itemView.context)
        }
    }

    fun interface OnClickListener {
        fun onClick(datetimeItemUiState: DatetimeItemUiState)
    }

    private companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<DatetimeItemUiState>() {
            override fun areItemsTheSame(
                oldItem: DatetimeItemUiState, newItem: DatetimeItemUiState
            ): Boolean {
                return oldItem.datetimeMillis == newItem.datetimeMillis
            }

            override fun areContentsTheSame(
                oldItem: DatetimeItemUiState, newItem: DatetimeItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}