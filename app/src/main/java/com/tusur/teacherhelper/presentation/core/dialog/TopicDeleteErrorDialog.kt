package com.tusur.teacherhelper.presentation.core.dialog

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tusur.teacherhelper.R

object TopicDeleteErrorDialog {

    fun show(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setMessage(R.string.dialog_topic_delete_error)
            .setPositiveButton(R.string.ok_button, null)
            .show()
    }

}