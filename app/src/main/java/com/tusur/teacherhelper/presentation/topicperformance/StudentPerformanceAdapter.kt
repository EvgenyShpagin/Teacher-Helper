package com.tusur.teacherhelper.presentation.topicperformance

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tusur.teacherhelper.R


class StudentPerformanceAdapter(
    private val onClickListener: OnClickListener
) : ListAdapter<GroupItemUiState, ViewHolder>(DiffUtilCallback()) {

    private val internalOnClickListener = InternalOnClickListener { position ->
        val item = getItem(position)
        if (item !is GroupItemUiState.Student) return@InternalOnClickListener
        onClickListener.onClick(item.studentId)
    }

    inner class StudentViewHolder(
        itemView: View,
        onClickListener: InternalOnClickListener
    ) : ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val label = itemView.findViewById<TextView>(R.id.label)
        private val imageView = itemView.findViewById<ImageView>(R.id.icon)

        init {
            itemView.setOnClickListener {
                onClickListener.onClick(adapterPosition)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(studentUiState: GroupItemUiState.Student) {
            val context = itemView.context
            title.text =
                "${studentUiState.ordinal}. ${studentUiState.studentName.toString(context)}"
            if (studentUiState.attendanceIconRes != null) {
                imageView.setImageResource(studentUiState.attendanceIconRes)
                imageView.isVisible = true
                label.isVisible = false
            } else {
                imageView.isVisible = false
                if (studentUiState.grade != null) {
                    label.text = studentUiState.grade.toString(context)
                    label.isVisible = true
                } else if (studentUiState.progress != null) {
                    label.text = studentUiState.progress.toString(context)
                    label.isVisible = true
                } else {
                    label.isVisible = false
                }
            }
        }

    }

    inner class LabelViewHolder(itemView: View) : ViewHolder(itemView) {
        private val textView = itemView as TextView

        fun bind(label: GroupItemUiState.Label) {
            textView.text = label.groupNumber
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupItemUiState.Label -> LABEL_VIEW_TYPE
            is GroupItemUiState.Student -> STUDENT_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        fun inflate(layoutRes: Int) = inflater.inflate(layoutRes, parent, false)
        return if (viewType == STUDENT_VIEW_TYPE) {
            StudentViewHolder(
                inflate(R.layout.list_item_student_performance),
                internalOnClickListener
            )
        } else {
            LabelViewHolder(inflate(R.layout.list_item_label))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is StudentViewHolder -> holder.bind(item as GroupItemUiState.Student)
            is LabelViewHolder -> holder.bind(item as GroupItemUiState.Label)
        }
    }

    fun interface OnClickListener {
        fun onClick(studentId: Int)
    }

    fun interface InternalOnClickListener {
        fun onClick(position: Int)
    }

    private companion object {

        const val LABEL_VIEW_TYPE = 0
        const val STUDENT_VIEW_TYPE = 1

        class DiffUtilCallback : DiffUtil.ItemCallback<GroupItemUiState>() {
            override fun areItemsTheSame(
                oldItem: GroupItemUiState, newItem: GroupItemUiState
            ): Boolean {
                return if (oldItem::class != newItem::class) {
                    false
                } else {
                    when (oldItem) {
                        is GroupItemUiState.Label -> {
                            newItem as GroupItemUiState.Label
                            oldItem.groupNumber == newItem.groupNumber
                        }

                        is GroupItemUiState.Student -> {
                            newItem as GroupItemUiState.Student
                            oldItem.studentId == newItem.studentId
                        }
                    }
                }
            }

            override fun areContentsTheSame(
                oldItem: GroupItemUiState, newItem: GroupItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}