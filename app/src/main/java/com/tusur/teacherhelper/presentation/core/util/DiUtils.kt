package com.tusur.teacherhelper.presentation.core.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import dagger.hilt.android.lifecycle.withCreationCallback

fun <VMF> Fragment.creationCallback(callback: (factory: VMF) -> ViewModel): CreationExtras {
    return defaultViewModelCreationExtras.withCreationCallback<VMF> { factory ->
        callback(factory)
    }
}