package com.tusur.teacherhelper.presentation.subjects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tusur.teacherhelper.R

class SubjectAdapter : ListAdapter<SubjectItemUiState, SubjectAdapter.SubjectViewHolder>(
    DiffUtilCallback()
) {

    var onClickListener: OnClickListener? = null


    inner class SubjectViewHolder(itemView: View) : ViewHolder(itemView) {
        private val textView = itemView as TextView

        init {
            itemView.setOnClickListener {
                onClickListener?.onClick(subjectId = getItem(adapterPosition).id)
            }
        }

        fun bind(subject: SubjectItemUiState) {
            textView.text = subject.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_text_and_next_arrow, parent, false)
        return SubjectViewHolder(view as TextView)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subject = getItem(position))
    }

    fun interface OnClickListener {
        fun onClick(subjectId: Int)
    }

    companion object {
        private class DiffUtilCallback : DiffUtil.ItemCallback<SubjectItemUiState>() {
            override fun areItemsTheSame(
                oldItem: SubjectItemUiState,
                newItem: SubjectItemUiState
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SubjectItemUiState,
                newItem: SubjectItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}