package com.tusur.teacherhelper.presentation.group

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.group.GroupStudentsViewModel.StudentItemUiState
import com.tusur.teacherhelper.presentation.util.hideKeyboard


class GroupStudentAdapter(
    private val onDeleteListener: OnDeleteListener
) : ListAdapter<StudentItemUiState, ViewHolder>(DiffUtilCallback()) {

    var isDeleting = false
        set(value) {
            if (value == field) return
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    inner class StudentViewHolder(itemView: View) : ViewHolder(itemView) {
        private val textView = itemView as TextView

        init {
            itemView.setOnClickListener {
                val item = getItem(adapterPosition)
                item.onEdit()
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(student: StudentItemUiState, studentOrdinal: Int) {
            textView.text = "$studentOrdinal. ${student.name}"
        }
    }

    inner class EditableStudentViewHolder(itemView: View) : ViewHolder(itemView) {
        private val textInputLayout = itemView.findViewById<TextInputLayout>(R.id.text_layout)
        private val textInputEditText = itemView.findViewById<TextInputEditText>(R.id.edit_text)

        init {
            textInputLayout.setEndIconOnClickListener {
                val student = getItem(adapterPosition)
                val currentText = textInputLayout.editText?.text?.toString() ?: ""
                student.onSave(currentText)
                textInputLayout.clearFocus()
            }

            textInputEditText.setOnEditorActionListener { v, actionId, event ->
                (actionId == EditorInfo.IME_ACTION_SEARCH).also {
                    if (it) {
                        v.hideKeyboard()
                    }
                }
            }
        }

        fun bind(student: StudentItemUiState) {
            textInputLayout.editText?.setText(student.name)
            if (student.errorMessage != null) {
                val errorText = student.errorMessage.toString(textInputLayout.context)
                textInputLayout.error = errorText
                textInputLayout.requestFocus()
            } else {
                textInputLayout.error = null
                textInputLayout.isErrorEnabled = false
            }
        }
    }

    inner class DeletableStudentViewHolder(itemView: View) : ViewHolder(itemView) {
        private val deleteButton = itemView.findViewById<ImageButton>(R.id.delete_button)
        private val textView = itemView.findViewById<TextView>(R.id.text_view)
        private var studentId: Int = 0

        init {
            deleteButton.setOnClickListener {
                val item = getItem(adapterPosition)
                onDeleteListener.onDelete(item.id)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(student: StudentItemUiState, studentOrdinal: Int) {
            studentId = student.id
            textView.text = "$studentOrdinal. ${student.name}"
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isDeleting) {
            DELETABLE_ITEM_VIEW_TYPE
        } else if (currentList[position].isEditing) {
            EDITABLE_ITEM_VIEW_TYPE
        } else {
            DEFAULT_ITEM_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        fun inflate(res: Int) = layoutInflater.inflate(res, parent, false)

        return when (viewType) {
            DEFAULT_ITEM_VIEW_TYPE -> StudentViewHolder(inflate(R.layout.list_item_student))
            EDITABLE_ITEM_VIEW_TYPE -> EditableStudentViewHolder(inflate(R.layout.list_item_student_editable))
            DELETABLE_ITEM_VIEW_TYPE -> DeletableStudentViewHolder(inflate(R.layout.list_item_text_and_delete_bt))
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = getItem(position)

        when (holder) {
            is StudentViewHolder -> holder.bind(student, position + 1)
            is EditableStudentViewHolder -> holder.bind(student)
            is DeletableStudentViewHolder -> holder.bind(student, position + 1)
        }
    }

    fun interface OnDeleteListener {
        fun onDelete(studentId: Int)
    }

    private companion object {

        const val DEFAULT_ITEM_VIEW_TYPE = 0
        const val EDITABLE_ITEM_VIEW_TYPE = 1
        const val DELETABLE_ITEM_VIEW_TYPE = 2

        class DiffUtilCallback : DiffUtil.ItemCallback<StudentItemUiState>() {
            override fun areItemsTheSame(
                oldItem: StudentItemUiState,
                newItem: StudentItemUiState
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: StudentItemUiState,
                newItem: StudentItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}