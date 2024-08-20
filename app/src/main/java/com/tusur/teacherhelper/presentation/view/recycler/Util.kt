package com.tusur.teacherhelper.presentation.view.recycler

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.checkNestedScrollState() {
    assert(adapter != null) { "Adapter must be not null" }
    val listSize = adapter!!.itemCount
    if (listSize == 0) {
        isNestedScrollingEnabled = false
        return
    }
    val linearLayoutManager = when (this.layoutManager) {
        is GridLayoutManager -> this.layoutManager as GridLayoutManager
        else -> this.layoutManager as LinearLayoutManager
    }
    val lastItemPosition = listSize - 1
    val firstVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
    val lastVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
    isNestedScrollingEnabled =
        firstVisibleItemPosition != 0 || lastVisibleItemPosition != lastItemPosition
}