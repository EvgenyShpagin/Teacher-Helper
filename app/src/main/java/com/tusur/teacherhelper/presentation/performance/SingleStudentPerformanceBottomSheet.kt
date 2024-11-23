package com.tusur.teacherhelper.presentation.performance

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.topicperformance.PerformanceBottomSheet
import com.tusur.teacherhelper.presentation.topicperformance.PerformanceViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SingleStudentPerformanceBottomSheet : PerformanceBottomSheet() {

    private val args: SingleStudentPerformanceBottomSheetArgs by navArgs()

    override val viewModel: PerformanceViewModel by viewModels(extrasProducer = {
        creationCallback<PerformanceViewModel.Factory> { factory ->
            factory.create(
                topicId = args.topicId,
                currentStudentId = args.studentId,
                datetimeMillis = args.datetimeMillis,
                allStudentIds = listOf(args.studentId)
            )
        }
    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.switchButtons.root.isVisible = false
        super.onViewCreated(view, savedInstanceState)
    }
}