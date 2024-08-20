package com.tusur.teacherhelper.presentation.util

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.domain.model.PerformanceItem
import com.tusur.teacherhelper.domain.model.SumProgress
import com.tusur.teacherhelper.domain.util.formatted
import com.tusur.teacherhelper.presentation.model.UiText
import com.tusur.teacherhelper.presentation.view.ListItemView
import com.tusur.teacherhelper.presentation.view.ListLayout
import com.tusur.teacherhelper.presentation.view.recycler.checkNestedScrollState
import com.tusur.teacherhelper.presentation.view.recycler.decorations.MarginItemDecoration
import java.util.Locale


val Resources.primaryLocale: Locale get() = configuration.locales.get(0)

fun getDefaultListItemDecoration(
    resources: Resources
): ItemDecoration {
    val horizontalMargin = resources.getDimension(R.dimen.fragment_horizontal_margin)
    val verticalMargin = resources.getDimension(R.dimen.default_list_item_vertical_margin)
    return MarginItemDecoration(
        verticalSpace = verticalMargin,
        horizontalSpace = horizontalMargin
    )
}

fun getGroupListItemDecoration(
    resources: Resources,
    addHorizontalSpace: Boolean = true
): ItemDecoration {
    val horizontalMargin = if (addHorizontalSpace) {
        resources.getDimension(R.dimen.fragment_horizontal_margin)
    } else {
        0f
    }
    val verticalMargin = resources.getDimension(R.dimen.group_list_item_vertical_margin)
    return MarginItemDecoration(
        verticalSpace = verticalMargin,
        horizontalSpace = horizontalMargin
    )
}

fun MenuItem.setTextColor(@ColorInt color: Int) {
    if (title.isNullOrBlank()) return
    title = SpannableString(title).also {
        it.setSpan(ForegroundColorSpan(color), 0, title!!.length, 0)
    }
}

fun Fragment.doOnBackPressed(topAppBar: Toolbar, action: () -> Unit) {
    topAppBar.setNavigationOnClickListener { action.invoke() }
    doOnBackPressed(action)
}

fun Fragment.doOnBackPressed(action: () -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback(this) {
        action.invoke()
    }
}

fun Fragment.hideKeyboard() {
    val windowController = WindowCompat.getInsetsController(requireActivity().window, requireView())
    windowController.hide(WindowInsetsCompat.Type.ime())
}

fun View.hideKeyboard() {
    findFragment<Fragment>().hideKeyboard()
}

fun Date.formatted(context: Context) = formatted(context.resources.primaryLocale)

fun List<Int>.toNativeArray(): IntArray {
    return IntArray(count()) { index -> get(index) }
}

fun PerformanceItem.toUiText(): UiText {
    return when (this) {
        PerformanceItem.Assessment.FAIL -> UiText.Resource(R.string.assessment_fail)
        PerformanceItem.Assessment.PASS -> UiText.Resource(R.string.assessment_pass)
        PerformanceItem.Attendance.Absent -> UiText.Resource(R.string.attendance_absent)
        PerformanceItem.Attendance.Excused -> UiText.Resource(R.string.attendance_excused)
        PerformanceItem.Attendance.Present -> UiText.Resource(R.string.attendance_present)
        is PerformanceItem.Grade ->
            if (value == 0) {
                UiText.Resource(R.string.performance_grade_0)
            } else {
                UiText.Dynamic(value.toString())
            }

        is PerformanceItem.Progress -> UiText.Dynamic(formatProgress(fraction = this.value))
    }
}

fun Drawable.withOtherColor(@ColorInt color: Int): Drawable {
    return DrawableCompat.wrap(this).also { DrawableCompat.setTint(it, color) }
}

fun formatProgress(fraction: Float): String {
    return formatProgress((fraction * 100).toInt())
}

fun formatProgress(percent: Int): String {
    return "${percent}%"
}

fun MaterialAlertDialogBuilder.setSingleChoiceItems(
    adapter: SingleChoiceAlertAdapter
): MaterialAlertDialogBuilder {
    return setSingleChoiceItems(adapter.itemsText, adapter.checkedPosition, adapter.dialogListener)
}

class SingleChoiceAlertAdapter(val items: List<Item>) {
    val itemsText = Array(items.count()) { i -> items[i].text }
    var checkedPosition = items.indexOfFirst { it.isChecked }.coerceAtLeast(0)
        private set
    val dialogListener = DialogInterface.OnClickListener { _, which ->
        val item = items[which]
        if (item.onClick != null) {
            item.onClick(which)
        } else {
            checkedPosition = which
        }
    }

    data class Item(
        val isChecked: Boolean,
        val text: String,
        val isEnabled: Boolean = true,
        val onClick: ((position: Int) -> Unit)? = null,
    )
}

fun AlertDialog.setDisabledItems(adapter: SingleChoiceAlertAdapter): AlertDialog {
    setOnShowListener {
        adapter.items.forEachIndexed { index, item ->
            if (!item.isEnabled) {
                listView[index].apply {
                    isEnabled = false
                    setOnClickListener { /* Do nothing */ }
                }
            }
        }
    }
    return this
}

fun TextInputEditText.doOnActionDone(action: (TextInputEditText) -> Unit) {
    setOnEditorActionListener { v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            action.invoke(v as TextInputEditText)
        }
        false
    }
}

fun TextInputEditText.clearFocusOnActionDone() {
    doOnActionDone { it.clearFocus() }
}

fun ListLayout.getListItemAt(index: Int): ListItemView? {
    var listItemIndex = 0
    for (i in 0 until childCount) {
        val view = getChildAt(i)
        if (view is ListItemView) {
            if (listItemIndex == index) {
                return view
            }
            ++listItemIndex
        }
    }
    return null
}

fun SumProgress<Number>.formatted(): UiText {
    return UiText.Resource(
        R.string.topic_progress_fraction,
        reached.toDouble().formatted(),
        total.toDouble().formatted()
    )
}

fun AppBarLayout.fixCollapsing(recyclerView: RecyclerView) {
    recyclerView.checkNestedScrollState()
    val needToExpandAppBar = !recyclerView.isNestedScrollingEnabled
    setExpanded(needToExpandAppBar)
}