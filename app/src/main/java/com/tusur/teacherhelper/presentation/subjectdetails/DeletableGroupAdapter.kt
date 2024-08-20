package com.tusur.teacherhelper.presentation.subjectdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tusur.teacherhelper.R

class DeletableGroupAdapter(private var onClickListener: OnClickListener) :
    ListAdapter<GroupItemUiState, DeletableGroupAdapter.BaseViewHolder>(DiffUtilCallback()) {

    private var uiState = UiState.DEFAULT

    override fun getItemViewType(position: Int): Int {
        return if (uiState == UiState.DEFAULT) {
            ItemViewType.GROUP_VIEW_TYPE.value
        } else {
            ItemViewType.DELETABLE_GROUP_VIEW_TYPE.value
        }

    }

    abstract inner class BaseViewHolder(itemView: View) : ViewHolder(itemView) {
        abstract fun bind(group: GroupItemUiState)
    }

    inner class GroupViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val textView = itemView as TextView

        init {
            itemView.setOnClickListener {
                onClickListener.onClick(getItem(adapterPosition).id)
            }
        }

        override fun bind(group: GroupItemUiState) {
            textView.text = group.number
        }
    }

    inner class DeletableGroupViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val textView = itemView.findViewById<TextView>(R.id.text_view)
        private val deleteButton = itemView.findViewById<ImageButton>(R.id.delete_button)

        init {
            deleteButton.setOnClickListener {
                onClickListener.onDeleteClick(getItem(adapterPosition).id)
            }
        }

        override fun bind(group: GroupItemUiState) {
            textView.text = group.number
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemViewType = getViewType(viewType)
        return itemViewType.getViewHolder(layoutInflater, parent)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(group = getItem(position))
    }

    fun showDeleteButtons() {
        if (uiState == UiState.DELETING) return
        uiState = UiState.DELETING
        notifyItemRangeChanged(0, itemCount)
    }

    fun hideDeleteButtons() {
        if (uiState != UiState.DELETING) return
        uiState = UiState.DEFAULT
        notifyItemRangeChanged(0, itemCount)
    }

    private fun getViewType(value: Int): ItemViewType {
        return ItemViewType.entries.find { it.value == value }!!
    }

    private fun ItemViewType.getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder {
        val view = layoutInflater.inflate(layoutRes, parent, false)
        return when (this) {
            ItemViewType.GROUP_VIEW_TYPE -> GroupViewHolder(view)
            ItemViewType.DELETABLE_GROUP_VIEW_TYPE -> DeletableGroupViewHolder(view)
        }
    }

    enum class UiState {
        DEFAULT,
        DELETING
    }

    interface OnClickListener {
        fun onClick(groupId: Int)
        fun onDeleteClick(groupId: Int)
    }

    enum class ItemViewType(val value: Int, @LayoutRes val layoutRes: Int) {
        GROUP_VIEW_TYPE(0, R.layout.list_item_text_and_next_arrow),
        DELETABLE_GROUP_VIEW_TYPE(1, R.layout.list_item_text_and_delete_bt),
    }

    companion object {
        private class DiffUtilCallback : DiffUtil.ItemCallback<GroupItemUiState>() {
            override fun areItemsTheSame(
                oldItem: GroupItemUiState, newItem: GroupItemUiState
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: GroupItemUiState, newItem: GroupItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}