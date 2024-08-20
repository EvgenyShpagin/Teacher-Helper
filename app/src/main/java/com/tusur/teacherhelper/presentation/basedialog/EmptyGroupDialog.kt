package com.tusur.teacherhelper.presentation.basedialog

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tusur.teacherhelper.R

class EmptyGroupDialog(
    context: Context,
    onConfirm: () -> Unit
) : MaterialAlertDialogBuilder(context) {

    init {
        setTitle(R.string.dialog_no_groups_title)
        setMessage(R.string.dialog_no_groups_body)
        setCancelable(false)
        setPositiveButton(R.string.back_button) { _, _ -> onConfirm() }
    }
}